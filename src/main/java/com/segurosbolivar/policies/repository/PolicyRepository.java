package com.segurosbolivar.policies.repository;

import com.segurosbolivar.policies.domain.Policy;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository {

    List<Policy> findAll();

    Optional<Policy> findById(Long id);

    Optional<Policy> findByRiskId(Long riskId);

    Policy save(Policy policy);

    long nextRiskId();
}
