package com.creatorhire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReportStatusRequest(
        @NotBlank @Pattern(regexp = "REVIEWED|DISMISSED", message = "unknown report status") String status) {
}
