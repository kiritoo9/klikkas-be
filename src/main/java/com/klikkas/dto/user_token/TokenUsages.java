package com.klikkas.dto.user_token;

import java.time.LocalDateTime;

public record TokenUsages(
    String usage_title,
    String llm_model,
    Integer input_token,
    Integer output_token,
    Integer estimated_cost,
    LocalDateTime created_at
) {}
