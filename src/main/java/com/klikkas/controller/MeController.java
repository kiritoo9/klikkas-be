package com.klikkas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.users.ChangePasswordRequest;
import com.klikkas.dto.users.UpdateProfileRequest;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.service.MeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final MeService meService;

    @GetMapping("/me")
    public UserResponse getMe() {
        return meService.getMe();
    }

    @PutMapping("/me/update_profile")
    public UserResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest req) {
        return meService.updateProfile(req);
    }

    @PutMapping("/me/change_password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest req) {
        meService.changePassword(req);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}