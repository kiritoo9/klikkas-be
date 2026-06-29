package com.klikkas.dto.cashflows;

public record SummarizeResponse(
    Integer initial_cash,
    Integer increasing_cash_amount,
    Integer final_cash
) {}
