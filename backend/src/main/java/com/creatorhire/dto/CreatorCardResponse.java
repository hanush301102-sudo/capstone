package com.creatorhire.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreatorCardResponse(
        Long id,
        String headline,
        String bio,
        Integer experienceYears,
        String availability,
        BigDecimal hourlyRate,
        BigDecimal onTimeDeliveryRate,
        int avgResponseTimeHours,
        int completedProjects,
        List<String> skills) {
}
