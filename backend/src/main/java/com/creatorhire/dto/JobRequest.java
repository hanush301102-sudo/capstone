package com.creatorhire.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record JobRequest(
        @NotBlank String title,
        String description,
        @NotBlank String creativeBrief,
        String styleKeywords,
        String referenceLinks,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        LocalDate deadline,
        @NotEmpty List<Long> skillIds) {
}
