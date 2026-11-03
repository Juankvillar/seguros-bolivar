package com.segurosbolivar.policies.service;

import com.segurosbolivar.policies.domain.PolicyStatus;
import com.segurosbolivar.policies.domain.Policy;
import com.segurosbolivar.policies.domain.PolicyType;
import com.segurosbolivar.policies.domain.Risk;
import com.segurosbolivar.policies.dto.PolicyResponse;
import com.segurosbolivar.policies.exception.BusinessRuleViolationException;
import com.segurosbolivar.policies.repository.InMemoryPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PolicyServiceTest {

    private CoreGateway coreGateway;
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        coreGateway = mock(CoreGateway.class);
        policyService = new PolicyService(new InMemoryPolicyRepository(), coreGateway, new BigDecimal("0.05"));
    }

    @Test
    void renewsPolicyUsingIpcAndRecalculatesPremium() {
        PolicyResponse renewed = policyService.renew(2L);

        assertThat(renewed.status()).isEqualTo(PolicyStatus.RENOVADA);
        assertThat(renewed.monthlyRent()).isEqualByComparingTo("1890000.00");
        assertThat(renewed.premium()).isEqualByComparingTo("22680000.00");
        verify(coreGateway).notifyPolicyUpdated(2L);
    }

    @Test
    void rejectsRenewalOfCancelledPolicy() {
        policyService.cancelPolicy(2L);

        assertThatThrownBy(() -> policyService.renew(2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("A cancelled policy cannot be renewed");
    }

    @Test
    void rejectsAddingRiskToIndividualPolicy() {
        assertThatThrownBy(() -> policyService.addRisk(2L, "Apartamento adicional"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Risks can only be added to collective policies");
    }

    @Test
    void assignsANewUniqueIdWhenAddingRisk() {
        var risk = policyService.addRisk(1L, "Apartamento 103");

        assertThat(risk.id()).isEqualTo(104L);
        verify(coreGateway).notifyPolicyUpdated(1L);
    }

    @Test
    void cancellingPolicyCancelsAllItsRisks() {
        policyService.cancelPolicy(1L);

        assertThat(policyService.findRisks(1L))
                .allSatisfy(risk -> assertThat(risk.status().name()).isEqualTo("CANCELADO"));
        verify(coreGateway).notifyPolicyUpdated(1L);
    }

    @Test
    void enforcesTheSingleRiskInvariantForIndividualPolicies() {
        assertThatThrownBy(() -> new Policy(
                99L,
                PolicyType.INDIVIDUAL,
                12,
                new BigDecimal("1000000.00"),
                List.of(new Risk(201L, "Apartamento 1"), new Risk(202L, "Apartamento 2"))
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An individual policy must have exactly one risk");
    }
}
