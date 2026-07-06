package com.klikkas.dto.cashflows;

public record CashflowDetailResponse(
    String category,
    Integer total,
    String type
) {}
