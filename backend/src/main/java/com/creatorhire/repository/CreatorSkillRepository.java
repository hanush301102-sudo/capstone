package com.creatorhire.repository;

import java.util.List;
import com.creatorhire.entity.CreatorSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorSkillRepository extends JpaRepository<CreatorSkill, Long> {

    List<CreatorSkill> findByCreatorProfileId(Long creatorProfileId);
}
