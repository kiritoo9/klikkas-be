package com.klikkas.dto.cashflows;

public record CashflowAccountDetail(
    String account_code,
    String account_name,
    String category,
    Integer amount,
    String type
) {}
