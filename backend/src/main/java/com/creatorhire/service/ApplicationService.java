package com.creatorhire.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.creatorhire.dto.ApplicationRequest;
import com.creatorhire.dto.ApplicationResponse;
import com.creatorhire.entity.Application;
import com.creatorhire.entity.ApplicationSample;
import com.creatorhire.entity.ApplicationStatus;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.JobStatus;
import com.creatorhire.entity.Notification;
import com.creatorhire.entity.Portfolio;
import com.creatorhire.entity.User;
import com.creatorhire.exception.ConflictException;
import com.creatorhire.exception.ForbiddenException;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.ApplicationRepository;
import com.creatorhire.repository.ApplicationSampleRepository;
import com.creatorhire.repository.ClientProfileRepository;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.JobRepository;
import com.creatorhire.repository.NotificationRepository;
import com.creatorhire.repository.PortfolioRepository;
import com.creatorhire.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private final ApplicationRepository applications;
    private final ApplicationSampleRepository samples;
    private final JobRepository jobs;
    private final CreatorProfileRepository creatorProfiles;
    private final PortfolioRepository portfolios;
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final ClientProfileRepository clientProfiles;
    private final MatchScoringService matchScoring;

    public ApplicationService(
            ApplicationRepository applications,
            ApplicationSampleRepository samples,
            JobRepository jobs,
            CreatorProfileRepository creatorProfiles,
            PortfolioRepository portfolios,
            NotificationRepository notifications,
            UserRepository users,
            ClientProfileRepository clientProfiles,
            MatchScoringService matchScoring) {
        this.applications = applications;
        this.samples = samples;
        this.jobs = jobs;
        this.creatorProfiles = creatorProfiles;
        this.portfolios = portfolios;
        this.notifications = notifications;
        this.users = users;
        this.clientProfiles = clientProfiles;
        this.matchScoring = matchScoring;
    }

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request) {
        CreatorProfile creator = currentCreator();
        Job job = jobs.findById(request.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + request.jobId()));
        if (job.getStatus() != JobStatus.OPEN) {
            throw new ConflictException("Job is not open for applications");
        }
        if (applications.existsByJobIdAndCreatorProfileId(job.getId(), creator.getId())) {
            throw new ConflictException("You have already applied for this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setCreatorProfile(creator);
        application.setCoverLetter(request.coverLetter());
        application.setBriefResponse(request.briefResponse());
        application.setProposedRate(request.proposedRate());
        application.setEstimatedDays(request.estimatedDays());
        application.setResponseTimeHours((int) ChronoUnit.HOURS.between(job.getCreatedAt(), LocalDateTime.now()));
        application.setMatchScore(matchScoring.score(application));
        applications.save(application);

        if (request.portfolioSampleIds() != null) {
            for (Long portfolioId : request.portfolioSampleIds()) {
                Portfolio portfolio = portfolios.findById(portfolioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
                if (!portfolio.getCreatorProfile().getId().equals(creator.getId())) {
                    throw new IllegalArgumentException("Samples must be your own portfolio items");
                }
                ApplicationSample sample = new ApplicationSample();
                sample.setApplication(application);
                sample.setPortfolio(portfolio);
                samples.save(sample);
            }
        }

        Notification notification = new Notification();
        notification.setUser(job.getClientProfile().getUser());
        notification.setType("APPLICATION_RECEIVED");
        notification.setMessage("New application for '" + job.getTitle()
                + "' with match score " + application.getMatchScore());
        notifications.save(notification);

        return toResponse(application);
    }

    /** Client view, ranked by match score. ACCEPT ships in TASK-015. */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> byJob(Long jobId) {
        Job job = jobs.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        if (!job.getClientProfile().getId().equals(currentClientId())) {
            throw new ForbiddenException("You do not own this job");
        }
        return applications.findByJobIdOrderByMatchScoreDesc(jobId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> mine() {
        return applications.findByCreatorProfileId(currentCreator().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, ApplicationStatus status) {
        if (status == ApplicationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Acceptance with project creation ships in TASK-015");
        }
        Application application = applications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
        if (!application.getJob().getClientProfile().getId().equals(currentClientId())) {
            throw new ForbiddenException("You do not own this job");
        }
        application.setStatus(status);

        Notification notification = new Notification();
        notification.setUser(application.getCreatorProfile().getUser());
        notification.setType(status.name());
        notification.setMessage("Your application for '"
                + application.getJob().getTitle() + "' was " + status.name().toLowerCase());
        notifications.save(notification);

        return toResponse(application);
    }

    private CreatorProfile currentCreator() {
        User user = currentUser();
        return creatorProfiles.findByUserId(user.getId())
                .orElseThrow(() -> new ForbiddenException("Creator profile required"));
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

    private ApplicationResponse toResponse(Application application) {
        List<Long> sampleIds = samples.findByApplicationId(application.getId()).stream()
                .map(s -> s.getPortfolio().getId())
                .toList();
        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getCreatorProfile().getId(),
                application.getCreatorProfile().getHeadline(),
                application.getCoverLetter(),
                application.getBriefResponse(),
                application.getProposedRate(),
                application.getEstimatedDays(),
                application.getMatchScore(),
                application.getResponseTimeHours(),
                application.getStatus().name(),
                application.getAppliedAt(),
                sampleIds);
    }
}
