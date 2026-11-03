package com.segurosbolivar.policies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CoreEventRequest(
        @NotBlank(message = "Event is required")
        @Pattern(regexp = "ACTUALIZACION", message = "Only ACTUALIZACION event is supported") String evento,
        @NotNull(message = "Policy id is required") Long polizaId
) {
}
