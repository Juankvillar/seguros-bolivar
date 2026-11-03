package com.segurosbolivar.policies.web;

import com.segurosbolivar.policies.dto.CoreEventRequest;
import com.segurosbolivar.policies.dto.CoreEventResponse;
import com.segurosbolivar.policies.service.CoreMockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private final CoreMockService coreMockService;

    public CoreMockController(CoreMockService coreMockService) {
        this.coreMockService = coreMockService;
    }

    @PostMapping("/evento")
    public CoreEventResponse registerEvent(@Valid @RequestBody CoreEventRequest request) {
        return coreMockService.registerAttempt(request);
    }
}
