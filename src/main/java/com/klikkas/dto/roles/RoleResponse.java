package com.klikkas.dto.roles;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoleResponse(
    UUID id,
    String name,
    String description,

    LocalDateTime createdAt
) {}