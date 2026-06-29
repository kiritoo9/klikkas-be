package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.cashflows.SummarizeResponse;
import com.klikkas.service.CashFlowService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
public class CashflowController {
    
    private final CashFlowService cashflow;

    @GetMapping("/cashflow/summarize")
    public SummarizeResponse getSummarize(
        @RequestParam(required = true) LocalDate dateFrom,
        @RequestParam(required = true) LocalDate dateTo
    ) {
        return cashflow.getSummarize(dateFrom, dateTo);
    }
    
}
