package com.klikkas.dto.users;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    
    @NotBlank(message = "Fullname is required")
    String fullname,
    
    // Optional
    String phone,
    String address,
    String remark
) {}
