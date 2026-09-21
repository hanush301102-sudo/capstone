package com.creatorhire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProjectStatusRequest(
        @NotBlank
                @Pattern(
                        regexp = "NOT_STARTED|IN_PROGRESS|COMPLETED|CANCELLED",
                        message = "unknown project status")
                String status) {
}
