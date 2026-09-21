package com.creatorhire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserStatusRequest(
        @NotBlank @Pattern(regexp = "ACTIVE|SUSPENDED|DEACTIVATED", message = "unknown user status")
                String status) {
}
