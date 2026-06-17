package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.klikkas.dto.auth.LoginRequest;
import com.klikkas.dto.auth.LoginResponse;
import com.klikkas.dto.auth.RegistRequest;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    public final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registration")
    public UserResponse regist(
            @Valid @RequestBody RegistRequest req) {
        return authService.regist(req);
    }

}
