package com.creatorhire.repository;

import java.util.List;
import java.util.Optional;
import com.creatorhire.entity.Application;
import com.creatorhire.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByJobId(Long jobId);

    /** Ranked view for clients: applications sorted by match score, highest first. */
    List<Application> findByJobIdOrderByMatchScoreDesc(Long jobId);

    List<Application> findByCreatorProfileId(Long creatorProfileId);

    List<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status);

    boolean existsByJobIdAndCreatorProfileId(Long jobId, Long creatorProfileId);

    Optional<Application> findByJobIdAndCreatorProfileId(Long jobId, Long creatorProfileId);
}
