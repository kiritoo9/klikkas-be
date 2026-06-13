package com.klikkas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.klikkas.service.HealthCheckService;
import com.klikkas.dto.HelloResponse;

@RestController
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/healthcheck")
    public HelloResponse healthCheck() {
        return healthCheckService.healthCheck();
    }
    
}
