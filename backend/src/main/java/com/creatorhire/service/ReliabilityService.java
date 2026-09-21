package com.creatorhire.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.creatorhire.entity.Application;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.Project;
import com.creatorhire.entity.ProjectStatus;
import com.creatorhire.repository.ApplicationRepository;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Derives a creator's reliability record from completed projects:
 * on-time delivery rate, average response time, completed project count.
 */
@Service
public class ReliabilityService {

    private final ProjectRepository projects;
    private final ApplicationRepository applications;
    private final CreatorProfileRepository creators;

    public ReliabilityService(
            ProjectRepository projects,
            ApplicationRepository applications,
            CreatorProfileRepository creators) {
        this.projects = projects;
        this.applications = applications;
        this.creators = creators;
    }

    @Transactional
    public void recalculate(Long creatorProfileId) {
        CreatorProfile creator = creators.findById(creatorProfileId).orElseThrow();
        List<Project> completed = projects.findByCreatorProfileId(creatorProfileId).stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .toList();

        creator.setCompletedProjects(completed.size());
        if (completed.isEmpty()) {
            creator.setOnTimeDeliveryRate(BigDecimal.ZERO);
        } else {
            long onTime = completed.stream().filter(this::isOnTime).count();
            creator.setOnTimeDeliveryRate(new BigDecimal("100")
                    .multiply(BigDecimal.valueOf(onTime))
                    .divide(BigDecimal.valueOf(completed.size()), 2, RoundingMode.HALF_UP));
        }

        List<Application> all = applications.findByCreatorProfileId(creatorProfileId);
        if (all.isEmpty()) {
            creator.setAvgResponseTimeHours(0);
        } else {
            double avg = all.stream().mapToInt(Application::getResponseTimeHours).average().orElse(0);
            creator.setAvgResponseTimeHours((int) Math.round(avg));
        }
        creators.save(creator);
    }

    private boolean isOnTime(Project project) {
        if (project.getDeadline() == null || project.getCompletedOn() == null) {
            return true;
        }
        return !project.getCompletedOn().isAfter(project.getDeadline());
    }
}
