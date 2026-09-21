package com.creatorhire.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;
import com.creatorhire.entity.Application;
import com.creatorhire.entity.Availability;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.Job;
import com.creatorhire.repository.CreatorSkillRepository;
import com.creatorhire.repository.JobSkillRepository;
import org.springframework.stereotype.Service;

/**
 * Transparent rule-based match scoring (0-100, no ML).
 *
 * <ul>
 *   <li>Skill overlap — 40 pts: share of required job skills the creator holds.</li>
 *   <li>Budget fit — 20 pts: proposed rate within budget range (or below min).</li>
 *   <li>Availability — 15 pts: AVAILABLE full, PARTIAL half, UNAVAILABLE zero.</li>
 *   <li>Deadline fit — 10 pts: estimated days fit within days until deadline.</li>
 *   <li>Reliability — 15 pts: on-time delivery rate (new creators get neutral half).</li>
 * </ul>
 */
@Service
public class MatchScoringService {

    private final JobSkillRepository jobSkills;
    private final CreatorSkillRepository creatorSkills;

    public MatchScoringService(JobSkillRepository jobSkills, CreatorSkillRepository creatorSkills) {
        this.jobSkills = jobSkills;
        this.creatorSkills = creatorSkills;
    }

    public BigDecimal score(Application application) {
        Job job = application.getJob();
        CreatorProfile creator = application.getCreatorProfile();

        BigDecimal total = BigDecimal.ZERO
                .add(skillOverlap(job, creator))
                .add(budgetFit(job, application))
                .add(availability(creator))
                .add(deadlineFit(job, application))
                .add(reliability(creator));

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal skillOverlap(Job job, CreatorProfile creator) {
        Set<Long> required = jobSkills.findByJobId(job.getId()).stream()
                .map(link -> link.getSkill().getId())
                .collect(Collectors.toSet());
        if (required.isEmpty()) {
            return new BigDecimal("40");
        }
        Set<Long> held = creatorSkills.findByCreatorProfileId(creator.getId()).stream()
                .map(link -> link.getSkill().getId())
                .collect(Collectors.toSet());
        held.retainAll(required);
        return new BigDecimal("40")
                .multiply(BigDecimal.valueOf(held.size()))
                .divide(BigDecimal.valueOf(required.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal budgetFit(Job job, Application application) {
        BigDecimal proposed = application.getProposedRate();
        if (proposed == null || job.getBudgetMin() == null || job.getBudgetMax() == null) {
            return new BigDecimal("10");
        }
        if (proposed.compareTo(job.getBudgetMin()) < 0) {
            return new BigDecimal("20");
        }
        if (proposed.compareTo(job.getBudgetMax()) <= 0) {
            return new BigDecimal("20");
        }
        BigDecimal over = proposed.subtract(job.getBudgetMax());
        BigDecimal range = job.getBudgetMax().subtract(job.getBudgetMin()).max(BigDecimal.ONE);
        BigDecimal penalty = over.divide(range, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("20"));
        return new BigDecimal("20").subtract(penalty).max(BigDecimal.ZERO);
    }

    private BigDecimal availability(CreatorProfile creator) {
        if (creator.getAvailability() == Availability.AVAILABLE) {
            return new BigDecimal("15");
        }
        if (creator.getAvailability() == Availability.PARTIAL) {
            return new BigDecimal("7.5");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal deadlineFit(Job job, Application application) {
        if (job.getDeadline() == null || application.getEstimatedDays() == null) {
            return new BigDecimal("5");
        }
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), job.getDeadline());
        return application.getEstimatedDays() <= daysLeft ? new BigDecimal("10") : BigDecimal.ZERO;
    }

    private BigDecimal reliability(CreatorProfile creator) {
        if (creator.getCompletedProjects() == 0) {
            return new BigDecimal("7.5");
        }
        return new BigDecimal("15")
                .multiply(creator.getOnTimeDeliveryRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
