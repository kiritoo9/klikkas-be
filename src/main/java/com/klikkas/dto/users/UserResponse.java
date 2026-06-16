package com.klikkas.dto.users;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    UUID role_id,
    String role_name,
    String email,
    String fullname,
    String phone,
    String address,
    String remark,
    LocalDateTime created_at
) {}