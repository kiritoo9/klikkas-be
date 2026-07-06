package com.klikkas.dto.cashflows;

import java.util.List;

public record CashflowListResponse(
        String account_code,
        String account_name,
        Integer total,
        List<CashflowDetailResponse> detail) {
}
