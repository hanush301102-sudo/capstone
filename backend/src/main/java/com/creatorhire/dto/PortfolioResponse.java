package com.creatorhire.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PortfolioResponse(
        Long id,
        Long creatorProfileId,
        String title,
        String description,
        String mediaUrl,
        String workType,
        String verificationStatus,
        String collaborationRole,
        String outcomeStats,
        LocalDateTime createdAt,
        List<String> skills) {
}
