package com.creatorhire.dto;

import jakarta.validation.constraints.Pattern;

public record CreatorSkillInput(
        Long skillId,
        @Pattern(regexp = "BEGINNER|INTERMEDIATE|EXPERT", message = "unknown skill level") String level) {
}
