package com.klikkas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.klikkas.dto.auth.LoginRequest;
import com.klikkas.dto.auth.LoginResponse;
import com.klikkas.entity.User;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    public final UserRepository userRepository;
    public final JwtService jwtService;
    public final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new NotFoundException(
                        "Invalid credential",
                        "INVALID_CREDENTIAL"));

        boolean validPassword = passwordEncoder.matches(
                request.password(),
                user.getPassword());

        if (!validPassword) {
            throw new BadRequestException("Invalid credential");
        }

        String accessToken = jwtService.generateToken(user, 1);
        String refreshToken = jwtService.generateToken(user, 14);

        return new LoginResponse(
            accessToken,
            refreshToken
        );
    }

}
