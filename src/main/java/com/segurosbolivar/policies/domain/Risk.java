package com.segurosbolivar.policies.domain;

import java.util.Objects;

public final class Risk {

    private final Long id;
    private final String description;
    private RiskStatus status;

    public Risk(Long id, String description) {
        this.id = Objects.requireNonNull(id, "Risk id is required");
        this.description = Objects.requireNonNull(description, "Risk description is required");
        this.status = RiskStatus.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public RiskStatus getStatus() {
        return status;
    }

    public void cancel() {
        status = RiskStatus.CANCELADO;
    }
}
