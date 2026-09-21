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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "creator_profiles")
public class CreatorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 255)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Availability availability = Availability.AVAILABLE;

    @Column(precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    /** Derived by ReliabilityService from completed projects (0-100). */
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal onTimeDeliveryRate = BigDecimal.ZERO;

    /** Derived by ReliabilityService; average hours between job post and application. */
    @Column(nullable = false)
    private int avgResponseTimeHours;

    /** Derived by ReliabilityService; count of COMPLETED projects. */
    @Column(nullable = false)
    private int completedProjects;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
