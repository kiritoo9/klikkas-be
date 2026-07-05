package com.klikkas.dto.auth;

public record UserLoginResponse(
        String email,
        String full_name,
        String role_name) {

}
