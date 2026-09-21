package com.creatorhire.dto;

import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long applicationId,
        String title,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate deadline,
        LocalDate completedOn) {
}
