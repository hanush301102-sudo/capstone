package com.creatorhire.repository;

import java.util.List;
import java.util.Optional;
import com.creatorhire.entity.Project;
import com.creatorhire.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByApplicationId(Long applicationId);

    List<Project> findByJobId(Long jobId);

    List<Project> findByStatus(ProjectStatus status);

    @Query("select p from Project p where p.application.creatorProfile.id = :creatorProfileId")
    List<Project> findByCreatorProfileId(Long creatorProfileId);
}
