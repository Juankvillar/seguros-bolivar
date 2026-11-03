package com.segurosbolivar.policies.repository;

import com.segurosbolivar.policies.domain.Policy;
import com.segurosbolivar.policies.domain.PolicyType;
import com.segurosbolivar.policies.domain.Risk;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPolicyRepository implements PolicyRepository {

    private final Map<Long, Policy> policies = new ConcurrentHashMap<>();
    private final AtomicLong riskSequence = new AtomicLong(103);

    public InMemoryPolicyRepository() {
        save(new Policy(
                1L,
                PolicyType.COLECTIVA,
                12,
                new BigDecimal("1250000.00"),
                List.of(new Risk(101L, "Apartamento 101"), new Risk(102L, "Apartamento 102"))
        ));
        save(new Policy(
                2L,
                PolicyType.INDIVIDUAL,
                12,
                new BigDecimal("1800000.00"),
                List.of(new Risk(103L, "Apartamento 501"))
        ));
    }

    @Override
    public List<Policy> findAll() {
        return policies.values().stream().toList();
    }

    @Override
    public Optional<Policy> findById(Long id) {
        return Optional.ofNullable(policies.get(id));
    }

    @Override
    public Optional<Policy> findByRiskId(Long riskId) {
        return policies.values().stream()
                .filter(policy -> policy.getRisks().stream().anyMatch(risk -> risk.getId().equals(riskId)))
                .findFirst();
    }

    @Override
    public Policy save(Policy policy) {
        policies.put(policy.getId(), policy);
        return policy;
    }

    @Override
    public long nextRiskId() {
        return riskSequence.incrementAndGet();
    }
}
