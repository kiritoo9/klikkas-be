package com.klikkas.service;

import org.springframework.stereotype.Service;

import com.klikkas.dto.HelloResponse;

@Service
public class HealthCheckService {

    public HelloResponse healthCheck() {
        return new HelloResponse(
            "KlikKas Service v0.1",
            "OK"
        );
    }
    
}
