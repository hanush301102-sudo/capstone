package com.creatorhire.repository;

import java.util.List;
import java.util.Optional;
import com.creatorhire.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    List<EmailOtp> findByUserIdAndUsedFalse(Long userId);
}
