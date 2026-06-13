package com.klikkas.dto.users;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullname,
    String phone,
    String address,
    String remark,
    LocalDateTime createdAt
) {}