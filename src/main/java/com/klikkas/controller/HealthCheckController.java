package com.klikkas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.klikkas.service.HealthCheckService;

import lombok.RequiredArgsConstructor;

import com.klikkas.dto.HelloResponse;
@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping("/healthcheck")
    public HelloResponse healthCheck() {
        return healthCheckService.healthCheck();
    }
    
}
