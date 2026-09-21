package com.creatorhire.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportRequest(
        Long reportedUserId,
        Long jobId,
        @NotBlank String reason,
        String description) {
}
