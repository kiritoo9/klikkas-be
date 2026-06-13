package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.klikkas.dto.users.CreateUserRequest;
import com.klikkas.dto.users.UserListResponse;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {

    public final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public UserListResponse getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "created_at") String order,
            @RequestParam(defaultValue = "desc") String dir) {
        return userService.getUsers(
                page,
                limit,
                order,
                dir);
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(
            @PathVariable UUID id) {
        return userService.getUser(id);
    }

    @PostMapping("/users")
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

}
