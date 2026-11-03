package com.segurosbolivar.policies.service;

import com.segurosbolivar.policies.dto.CoreEventRequest;
import com.segurosbolivar.policies.dto.CoreEventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CoreMockService implements CoreGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreMockService.class);

    @Override
    public void notifyPolicyUpdated(Long policyId) {
        registerAttempt(new CoreEventRequest("ACTUALIZACION", policyId));
    }

    public CoreEventResponse registerAttempt(CoreEventRequest request) {
        LOGGER.info("CORE update attempt registered: event={}, policyId={}", request.evento(), request.polizaId());
        return new CoreEventResponse("CORE update attempt registered", request.evento(), request.polizaId());
    }
}
