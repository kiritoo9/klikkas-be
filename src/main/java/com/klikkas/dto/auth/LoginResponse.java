package com.klikkas.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserLoginResponse user) {
}
