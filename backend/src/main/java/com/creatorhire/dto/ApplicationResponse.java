package com.creatorhire.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long creatorProfileId,
        String creatorHeadline,
        String coverLetter,
        String briefResponse,
        BigDecimal proposedRate,
        Integer estimatedDays,
        BigDecimal matchScore,
        int responseTimeHours,
        String status,
        LocalDateTime appliedAt,
        List<Long> samplePortfolioIds) {
}
