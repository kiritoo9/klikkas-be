package com.klikkas.dto.cashflows;

public record SumByCategory(
    Long grandTotal,
    String orderType,
    String name
) {}
