package com.creatorhire.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JobResponse(
        Long id,
        String title,
        String description,
        String creativeBrief,
        String styleKeywords,
        String referenceLinks,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        LocalDate deadline,
        String status,
        LocalDateTime createdAt,
        Long clientProfileId,
        String companyName,
        List<String> skills,
        long applicationCount) {
}
