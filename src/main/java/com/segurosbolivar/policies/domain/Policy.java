package com.segurosbolivar.policies.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Policy {

    private static final int MONEY_SCALE = 2;

    private final Long id;
    private final PolicyType type;
    private final int initialTermMonths;
    private final List<Risk> risks = new ArrayList<>();
    private BigDecimal monthlyRent;
    private BigDecimal premium;
    private PolicyStatus status;

    public Policy(Long id, PolicyType type, int initialTermMonths, BigDecimal monthlyRent, List<Risk> initialRisks) {
        this.id = Objects.requireNonNull(id, "Policy id is required");
        this.type = Objects.requireNonNull(type, "Policy type is required");
        if (initialTermMonths <= 0) {
            throw new IllegalArgumentException("Initial term must be greater than zero");
        }
        this.initialTermMonths = initialTermMonths;
        this.monthlyRent = normalizeMoney(monthlyRent);
        this.premium = calculatePremium(this.monthlyRent);
        this.status = PolicyStatus.ACTIVA;
        List<Risk> validatedRisks = List.copyOf(Objects.requireNonNull(initialRisks, "Initial risks are required"));
        validateInitialRisks(validatedRisks);
        this.risks.addAll(validatedRisks);
    }

    public Long getId() {
        return id;
    }

    public PolicyType getType() {
        return type;
    }

    public int getInitialTermMonths() {
        return initialTermMonths;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public List<Risk> getRisks() {
        return List.copyOf(risks);
    }

    public void renew(BigDecimal ipcRate) {
        monthlyRent = normalizeMoney(monthlyRent.multiply(BigDecimal.ONE.add(ipcRate)));
        premium = calculatePremium(monthlyRent);
        status = PolicyStatus.RENOVADA;
    }

    public void cancel() {
        status = PolicyStatus.CANCELADA;
        risks.forEach(Risk::cancel);
    }

    public void addRisk(Risk risk) {
        if (type != PolicyType.COLECTIVA) {
            throw new IllegalStateException("Risks can only be added to collective policies");
        }
        risks.add(Objects.requireNonNull(risk, "Risk is required"));
    }

    private void validateInitialRisks(List<Risk> initialRisks) {
        if (type == PolicyType.INDIVIDUAL && initialRisks.size() != 1) {
            throw new IllegalArgumentException("An individual policy must have exactly one risk");
        }
        if (type == PolicyType.COLECTIVA && initialRisks.isEmpty()) {
            throw new IllegalArgumentException("A collective policy must have at least one risk");
        }
    }

    private BigDecimal calculatePremium(BigDecimal rent) {
        return normalizeMoney(rent.multiply(BigDecimal.valueOf(initialTermMonths)));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return Objects.requireNonNull(value, "Monthly rent is required").setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
