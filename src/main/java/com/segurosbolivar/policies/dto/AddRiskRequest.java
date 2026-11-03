package com.segurosbolivar.policies.dto;

import jakarta.validation.constraints.NotBlank;

public record AddRiskRequest(@NotBlank(message = "Risk description is required") String description) {
}
