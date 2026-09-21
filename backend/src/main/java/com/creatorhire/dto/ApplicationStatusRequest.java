package com.creatorhire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ApplicationStatusRequest(
        @NotBlank
                @Pattern(
                        regexp = "SHORTLISTED|REJECTED",
                        message = "status must be SHORTLISTED or REJECTED (acceptance ships in TASK-015)")
                String status) {
}
