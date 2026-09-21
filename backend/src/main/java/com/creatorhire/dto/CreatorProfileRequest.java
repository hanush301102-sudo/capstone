package com.creatorhire.dto;

import java.util.List;
import jakarta.validation.constraints.Pattern;

public record CreatorProfileRequest(
        String headline,
        String bio,
        Integer experienceYears,
        @Pattern(regexp = "AVAILABLE|PARTIAL|UNAVAILABLE", message = "unknown availability") String availability,
        java.math.BigDecimal hourlyRate,
        List<CreatorSkillInput> skills) {
}
