package com.creatorhire.repository;

import java.util.List;
import java.util.Optional;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByName(String name);

    List<Skill> findByCategory(SkillCategory category);
}
