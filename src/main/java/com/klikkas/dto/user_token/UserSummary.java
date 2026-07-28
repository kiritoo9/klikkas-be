package com.klikkas.dto.user_token;

import java.time.LocalDateTime;

public record UserSummary(
    Integer limit_token,
    Integer usage_token,
    Integer estimated_cost,
    LocalDateTime reset_at
) {}
