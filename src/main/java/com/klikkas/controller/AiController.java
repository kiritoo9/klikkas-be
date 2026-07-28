package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.ai.FinanceHealth;
import com.klikkas.service.AiService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/ai/finance_health_check")
    public FinanceHealth checkFinanceHealth() {
        return aiService.checkFinanceHealth();
    }
    
    
}
