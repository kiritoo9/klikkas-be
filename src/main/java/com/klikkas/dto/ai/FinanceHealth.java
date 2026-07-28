package com.klikkas.dto.ai;

public record FinanceHealth(
    Boolean ai_detection_success,
    Integer score,
    String summary_message,
    String suggest_message, 
    String solution_message
) {}
