package com.creatorhire.repository;

import java.util.List;
import com.creatorhire.entity.Portfolio;
import com.creatorhire.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByCreatorProfileId(Long creatorProfileId);

    List<Portfolio> findByVerificationStatus(VerificationStatus status);
}
