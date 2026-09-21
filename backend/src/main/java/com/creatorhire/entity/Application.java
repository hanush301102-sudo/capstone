package com.creatorhire.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "creator_profile_id"}))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_profile_id", nullable = false)
    private CreatorProfile creatorProfile;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    /** Tailored answer to the job's creative brief. */
    @Column(columnDefinition = "TEXT")
    private String briefResponse;

    @Column(precision = 12, scale = 2)
    private BigDecimal proposedRate;

    private Integer estimatedDays;

    /** Rule-based score computed by MatchScoringService (0-100). */
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal matchScore = BigDecimal.ZERO;

    /** Hours between job posting and this application; feeds reliability. */
    @Column(nullable = false)
    private int responseTimeHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    void onCreate() {
        appliedAt = LocalDateTime.now();
    }
}
