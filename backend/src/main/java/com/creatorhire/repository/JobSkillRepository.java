package com.creatorhire.repository;

import java.util.List;
import com.creatorhire.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    List<JobSkill> findByJobId(Long jobId);
}
