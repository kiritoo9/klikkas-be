package com.klikkas.dto.users;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullname
) {}