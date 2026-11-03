package com.segurosbolivar.policies.web;

import com.segurosbolivar.policies.domain.PolicyStatus;
import com.segurosbolivar.policies.domain.PolicyType;
import com.segurosbolivar.policies.dto.AddRiskRequest;
import com.segurosbolivar.policies.dto.PolicyResponse;
import com.segurosbolivar.policies.dto.RiskResponse;
import com.segurosbolivar.policies.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/polizas")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public List<PolicyResponse> list(@RequestParam(required = false) PolicyType tipo,
                                     @RequestParam(required = false) PolicyStatus estado) {
        return policyService.list(tipo, estado);
    }

    @GetMapping("/{id}/riesgos")
    public List<RiskResponse> findRisks(@PathVariable Long id) {
        return policyService.findRisks(id);
    }

    @PostMapping("/{id}/renovar")
    public PolicyResponse renew(@PathVariable Long id) {
        return policyService.renew(id);
    }

    @PostMapping("/{id}/cancelar")
    public PolicyResponse cancel(@PathVariable Long id) {
        return policyService.cancelPolicy(id);
    }

    @PostMapping("/{id}/riesgos")
    @ResponseStatus(HttpStatus.CREATED)
    public RiskResponse addRisk(@PathVariable Long id, @Valid @RequestBody AddRiskRequest request) {
        return policyService.addRisk(id, request.description());
    }
}
