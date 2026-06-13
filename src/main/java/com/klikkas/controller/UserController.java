package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.klikkas.dto.users.UserListResponse;
import com.klikkas.service.UserService;

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
        @RequestParam(required = false) String order,
        @RequestParam(defaultValue = "asc") String dir
    ) {
        return userService.getUsers(
            page,
            limit,
            order,
            dir
        );
    } 
    
}
