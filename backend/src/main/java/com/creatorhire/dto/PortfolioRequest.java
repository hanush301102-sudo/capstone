package com.creatorhire.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

public record PortfolioRequest(
        @NotBlank String title,
        String description,
        String mediaUrl,
        String workType,
        String collaborationRole,
        String outcomeStats,
        List<Long> skillIds) {
}
