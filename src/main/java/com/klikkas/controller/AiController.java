package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.ai.FinanceHealth;
import com.klikkas.dto.ai.GenReportRequest;
import com.klikkas.dto.ai.GenReportResponse;
import com.klikkas.service.AiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/ai/finance_health_check")
    public FinanceHealth checkFinanceHealth() {
        return aiService.checkFinanceHealth();
    }

    @PostMapping("/ai/generative_report")
    public GenReportResponse genReportAi(
        @Valid @RequestBody GenReportRequest req
    ) {
        return aiService.generativeReport(req);
    }
    
}
