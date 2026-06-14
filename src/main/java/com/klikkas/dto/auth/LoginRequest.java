package com.klikkas.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank(message = "Email is required")
    @Email(message = "Format email invalid")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}
