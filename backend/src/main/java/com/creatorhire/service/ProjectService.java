package com.creatorhire.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.creatorhire.dto.ProjectResponse;
import com.creatorhire.entity.Application;
import com.creatorhire.entity.ApplicationStatus;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.Notification;
import com.creatorhire.entity.Project;
import com.creatorhire.entity.ProjectStatus;
import com.creatorhire.entity.User;
import com.creatorhire.exception.ConflictException;
import com.creatorhire.exception.ForbiddenException;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.ApplicationRepository;
import com.creatorhire.repository.ClientProfileRepository;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.JobRepository;
import com.creatorhire.repository.NotificationRepository;
import com.creatorhire.repository.ProjectRepository;
import com.creatorhire.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final ApplicationRepository applications;
    private final JobRepository jobs;
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final ClientProfileRepository clientProfiles;
    private final CreatorProfileRepository creatorProfiles;
    private final ReliabilityService reliability;

    public ProjectService(
            ProjectRepository projects,
            ApplicationRepository applications,
            JobRepository jobs,
            NotificationRepository notifications,
            UserRepository users,
            ClientProfileRepository clientProfiles,
            CreatorProfileRepository creatorProfiles,
            ReliabilityService reliability) {
        this.projects = projects;
        this.applications = applications;
        this.jobs = jobs;
        this.notifications = notifications;
        this.users = users;
        this.clientProfiles = clientProfiles;
        this.creatorProfiles = creatorProfiles;
        this.reliability = reliability;
    }

    /**
     * Hiring: accepts an application, creates the project, and auto-rejects
     * all other applications for the same job.
     */
    @Transactional
    public ProjectResponse accept(Long applicationId) {
        Application application = applications.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        Job job = application.getJob();
        if (!job.getClientProfile().getId().equals(currentClientId())) {
            throw new ForbiddenException("You do not own this job");
        }
        if (application.getStatus() == ApplicationStatus.ACCEPTED) {
            throw new ConflictException("Application is already accepted");
        }
        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new ConflictException("Rejected applications cannot be accepted");
        }
        if (projects.findByApplicationId(applicationId).isPresent()) {
            throw new ConflictException("A project already exists for this application");
        }

        application.setStatus(ApplicationStatus.ACCEPTED);

        Project project = new Project();
        project.setJob(job);
        project.setApplication(application);
        project.setTitle(job.getTitle());
        project.setStatus(ProjectStatus.NOT_STARTED);
        project.setDeadline(job.getDeadline());
        projects.save(project);

        notifyUser(
                application.getCreatorProfile().getUser(),
                "ACCEPTED",
                "You were hired for '" + job.getTitle() + "' — project created");

        for (Application other : applications.findByJobId(job.getId())) {
            if (!other.getId().equals(applicationId) && other.getStatus() != ApplicationStatus.REJECTED) {
                other.setStatus(ApplicationStatus.REJECTED);
                notifyUser(
                        other.getCreatorProfile().getUser(),
                        "REJECTED",
                        "Your application for '" + job.getTitle() + "' was rejected");
            }
        }
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateStatus(Long projectId, ProjectStatus status) {
        Project project = partyProject(projectId);
        if (!isTransitionAllowed(project.getStatus(), status)) {
            throw new IllegalArgumentException(
                    "Cannot move project from " + project.getStatus() + " to " + status);
        }
        project.setStatus(status);
        if (status == ProjectStatus.IN_PROGRESS && project.getStartDate() == null) {
            project.setStartDate(LocalDate.now());
        }
        if (status == ProjectStatus.COMPLETED) {
            project.setCompletedOn(LocalDate.now());
            project.setEndDate(LocalDate.now());
        }
        if (status == ProjectStatus.CANCELLED) {
            project.setEndDate(LocalDate.now());
        }

        User client = project.getJob().getClientProfile().getUser();
        User creator = project.getApplication().getCreatorProfile().getUser();
        notifyUser(client, "PROJECT_" + status.name(), "Project '" + project.getTitle() + "' is now " + status.name());
        notifyUser(creator, "PROJECT_" + status.name(), "Project '" + project.getTitle() + "' is now " + status.name());

        if (status == ProjectStatus.COMPLETED) {
            reliability.recalculate(project.getApplication().getCreatorProfile().getId());
        }
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long projectId) {
        return toResponse(partyProject(projectId));
    }

    /** Role-aware: client's hired projects or creator's hired projects. */
    @Transactional(readOnly = true)
    public List<ProjectResponse> mine() {
        User user = currentUser();
        List<Project> result = new ArrayList<>();
        clientProfiles
                .findByUserId(user.getId())
                .ifPresent(client -> jobs.findByClientProfileId(client.getId())
                        .forEach(job -> result.addAll(projects.findByJobId(job.getId()))));
        creatorProfiles
                .findByUserId(user.getId())
                .ifPresent(creator -> applications.findByCreatorProfileId(creator.getId())
                        .forEach(app -> projects.findByApplicationId(app.getId()).ifPresent(result::add)));
        return result.stream().map(this::toResponse).toList();
    }

    private boolean isTransitionAllowed(ProjectStatus from, ProjectStatus to) {
        if (from == to) {
            return true;
        }
        return switch (from) {
            case NOT_STARTED -> to == ProjectStatus.IN_PROGRESS || to == ProjectStatus.CANCELLED;
            case IN_PROGRESS -> to == ProjectStatus.COMPLETED || to == ProjectStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private Project partyProject(Long projectId) {
        Project project = projects.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User user = currentUser();
        boolean isClient = project.getJob().getClientProfile().getUser().getId().equals(user.getId());
        boolean isCreator = project.getApplication().getCreatorProfile().getUser().getId().equals(user.getId());
        if (!isClient && !isCreator) {
            throw new ForbiddenException("You are not a party to this project");
        }
        return project;
    }

    private Long currentClientId() {
        User user = currentUser();
        return clientProfiles.findByUserId(user.getId())
                .orElseThrow(() -> new ForbiddenException("Client profile required"))
                .getId();
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void notifyUser(User user, String type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notifications.save(notification);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getJob().getId(),
                project.getJob().getTitle(),
                project.getApplication().getId(),
                project.getTitle(),
                project.getStatus().name(),
                project.getStartDate(),
                project.getEndDate(),
                project.getDeadline(),
                project.getCompletedOn());
    }
}
