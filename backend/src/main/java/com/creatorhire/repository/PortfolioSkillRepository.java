package com.creatorhire.repository;

import java.util.List;
import com.creatorhire.entity.PortfolioSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioSkillRepository extends JpaRepository<PortfolioSkill, Long> {

    List<PortfolioSkill> findByPortfolioId(Long portfolioId);
}
