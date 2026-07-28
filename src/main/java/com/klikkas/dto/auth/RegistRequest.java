package com.klikkas.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RegistRequest(
    
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "Fullname is required")
    String fullname,

    @NotBlank(message = "Password is required")
    String password,

    String phone,
    String address,

    String google_id
) {}
