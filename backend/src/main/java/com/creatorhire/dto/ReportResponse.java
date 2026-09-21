package com.creatorhire.dto;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        String reporterEmail,
        Long reportedUserId,
        Long jobId,
        String reason,
        String description,
        String status,
        LocalDateTime createdAt) {
}
