package com.segurosbolivar.policies.dto;

import com.segurosbolivar.policies.domain.Policy;
import com.segurosbolivar.policies.domain.PolicyStatus;
import com.segurosbolivar.policies.domain.PolicyType;

import java.math.BigDecimal;

public record PolicyResponse(
        Long id,
        PolicyType type,
        PolicyStatus status,
        int initialTermMonths,
        BigDecimal monthlyRent,
        BigDecimal premium,
        int riskCount
) {
    public static PolicyResponse from(Policy policy) {
        return new PolicyResponse(
                policy.getId(),
                policy.getType(),
                policy.getStatus(),
                policy.getInitialTermMonths(),
                policy.getMonthlyRent(),
                policy.getPremium(),
                policy.getRisks().size()
        );
    }
}
