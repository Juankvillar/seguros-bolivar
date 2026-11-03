package com.segurosbolivar.policies.web;

import com.segurosbolivar.policies.dto.RiskResponse;
import com.segurosbolivar.policies.service.PolicyService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riesgos")
public class RiskController {

    private final PolicyService policyService;

    public RiskController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/{id}/cancelar")
    public RiskResponse cancel(@PathVariable Long id) {
        return policyService.cancelRisk(id);
    }
}
