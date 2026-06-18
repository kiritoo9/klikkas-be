package com.klikkas.dto.users;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    
    @NotBlank(message = "Old password is required")
    String old_password,
    
    @NotBlank(message = "New password is required")
    String new_password,
    
    @NotBlank(message = "Confirm password is required")
    String confirm_password
) {}
