package com.segurosbolivar.policies.service;

import com.segurosbolivar.policies.domain.Policy;
import com.segurosbolivar.policies.domain.PolicyStatus;
import com.segurosbolivar.policies.domain.PolicyType;
import com.segurosbolivar.policies.domain.Risk;
import com.segurosbolivar.policies.dto.PolicyResponse;
import com.segurosbolivar.policies.dto.RiskResponse;
import com.segurosbolivar.policies.exception.BusinessRuleViolationException;
import com.segurosbolivar.policies.exception.ResourceNotFoundException;
import com.segurosbolivar.policies.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final CoreGateway coreGateway;
    private final BigDecimal defaultIpcRate;

    public PolicyService(PolicyRepository policyRepository,
                         CoreGateway coreGateway,
                         @Value("${app.policy.ipc-rate}") BigDecimal defaultIpcRate) {
        this.policyRepository = policyRepository;
        this.coreGateway = coreGateway;
        this.defaultIpcRate = defaultIpcRate;
    }

    public List<PolicyResponse> list(PolicyType type, PolicyStatus status) {
        return policyRepository.findAll().stream()
                .filter(policy -> type == null || policy.getType() == type)
                .filter(policy -> status == null || policy.getStatus() == status)
                .map(PolicyResponse::from)
                .toList();
    }

    public List<RiskResponse> findRisks(Long policyId) {
        return getPolicy(policyId).getRisks().stream().map(RiskResponse::from).toList();
    }

    public synchronized PolicyResponse renew(Long policyId) {
        Policy policy = getPolicy(policyId);
        if (policy.getStatus() == PolicyStatus.CANCELADA) {
            throw new BusinessRuleViolationException("A cancelled policy cannot be renewed");
        }

        if (defaultIpcRate.signum() < 0) {
            throw new BusinessRuleViolationException("IPC rate cannot be negative");
        }

        policy.renew(defaultIpcRate);
        policyRepository.save(policy);
        coreGateway.notifyPolicyUpdated(policyId);
        return PolicyResponse.from(policy);
    }

    public synchronized PolicyResponse cancelPolicy(Long policyId) {
        Policy policy = getPolicy(policyId);
        policy.cancel();
        policyRepository.save(policy);
        coreGateway.notifyPolicyUpdated(policyId);
        return PolicyResponse.from(policy);
    }

    public synchronized RiskResponse addRisk(Long policyId, String description) {
        Policy policy = getPolicy(policyId);
        if (policy.getType() != PolicyType.COLECTIVA) {
            throw new BusinessRuleViolationException("Risks can only be added to collective policies");
        }
        if (policy.getStatus() == PolicyStatus.CANCELADA) {
            throw new BusinessRuleViolationException("A risk cannot be added to a cancelled policy");
        }

        Risk risk = new Risk(policyRepository.nextRiskId(), description);
        policy.addRisk(risk);
        policyRepository.save(policy);
        coreGateway.notifyPolicyUpdated(policyId);
        return RiskResponse.from(risk);
    }

    public synchronized RiskResponse cancelRisk(Long riskId) {
        Policy policy = policyRepository.findByRiskId(riskId)
                .orElseThrow(() -> new ResourceNotFoundException("Risk with id %s was not found".formatted(riskId)));
        Risk risk = policy.getRisks().stream()
                .filter(candidate -> candidate.getId().equals(riskId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Risk with id %s was not found".formatted(riskId)));

        risk.cancel();
        policyRepository.save(policy);
        coreGateway.notifyPolicyUpdated(policy.getId());
        return RiskResponse.from(risk);
    }

    private Policy getPolicy(Long policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy with id %s was not found".formatted(policyId)));
    }
}
