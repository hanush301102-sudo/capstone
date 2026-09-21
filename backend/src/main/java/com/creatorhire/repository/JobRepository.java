package com.creatorhire.repository;

import java.util.List;
import java.util.Optional;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByClientProfileId(Long clientProfileId);

    List<Job> findByStatus(JobStatus status);

    /**
     * Creator job discovery: open jobs requiring at least one of the given
     * skills, optionally filtered by keyword in title/description/brief.
     */
    @Query("""
            select distinct j from Job j
            join JobSkill js on js.job = j
            where j.status = :status
            and js.skill.id in :skillIds
            and (:keyword is null
                 or lower(j.title) like lower(concat('%', :keyword, '%'))
                 or lower(j.description) like lower(concat('%', :keyword, '%'))
                 or lower(j.creativeBrief) like lower(concat('%', :keyword, '%')))
            """)
    List<Job> searchOpenJobs(List<Long> skillIds, String keyword, JobStatus status);
}
