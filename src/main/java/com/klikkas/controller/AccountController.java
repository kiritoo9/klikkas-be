package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.klikkas.dto.accounts.AccountListResponse;
import com.klikkas.dto.accounts.AccountResponse;
import com.klikkas.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {

    public final AccountService accountService;

    @GetMapping("/accounts")
    public AccountListResponse getAccounts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "code") String order,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String keywords) {
        return accountService.getAccounts(
                page,
                limit,
                order,
                dir,
                keywords);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(
            @PathVariable UUID id) {
        return accountService.getAccount(id);
    }

}
