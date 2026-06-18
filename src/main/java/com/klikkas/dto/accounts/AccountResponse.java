package com.klikkas.dto.accounts;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String code,
    String name,
    String type,
    UUID parent_id,
    Boolean is_active,
    LocalDateTime created_at
) {}
