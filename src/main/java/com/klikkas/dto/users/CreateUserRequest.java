package com.klikkas.dto.users;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotNull(message = "Role ID is required")
    UUID role_id,

    @NotBlank(message = "Fullname is required")
    String fullname,

    @NotBlank(message = "password is required")
    String password,

    // Optional
    String phone,
    String address,
    String remark
) {}
