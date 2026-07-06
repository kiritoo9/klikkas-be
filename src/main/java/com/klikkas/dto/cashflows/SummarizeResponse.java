package com.klikkas.dto.cashflows;

import java.time.LocalDateTime;

public record SummarizeResponse(
        Integer initial_cash,
        LocalDateTime periode,
        Integer increasing_cash_amount,
        Integer final_cash) {
}
