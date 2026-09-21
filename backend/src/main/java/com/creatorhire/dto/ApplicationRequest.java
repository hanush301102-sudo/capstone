package com.creatorhire.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotNull Long jobId,
        String coverLetter,
        String briefResponse,
        BigDecimal proposedRate,
        Integer estimatedDays,
        List<Long> portfolioSampleIds) {
}
