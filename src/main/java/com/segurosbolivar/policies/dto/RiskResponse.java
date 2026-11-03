package com.segurosbolivar.policies.dto;

import com.segurosbolivar.policies.domain.Risk;
import com.segurosbolivar.policies.domain.RiskStatus;

public record RiskResponse(Long id, String description, RiskStatus status) {
    public static RiskResponse from(Risk risk) {
        return new RiskResponse(risk.getId(), risk.getDescription(), risk.getStatus());
    }
}
