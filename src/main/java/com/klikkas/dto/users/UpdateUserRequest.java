package com.klikkas.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Fullname is required")
    String fullname,
    
    // Optional
    String password,
    String phone,
    String address,
    String remark
) {}
