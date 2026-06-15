package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.roles.RoleListResponse;
import com.klikkas.service.RoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/roles")
    public RoleListResponse getRoles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "created_at") String order,
            @RequestParam(defaultValue = "desc") String dir,
            @RequestParam(required = false) String keywords) {
        return roleService.getRoles(page, limit, order, dir, keywords);
    }

}
