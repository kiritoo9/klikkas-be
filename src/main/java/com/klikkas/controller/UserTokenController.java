package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.user_token.TokenUsageList;
import com.klikkas.dto.user_token.UserSummary;
import com.klikkas.service.UserTokenService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
public class UserTokenController {

    private final UserTokenService userTokenService;

    @GetMapping("/token/summary")
    public UserSummary getSummaryToken() {
        return userTokenService.getSummaryToken();
    }

    @GetMapping("/token/usages")
    public TokenUsageList getTokenUsages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "createdAt", required = false) String order,
            @RequestParam(defaultValue = "desc", required = false) String dir,
            @RequestParam(required = false) String keywords) {
        return userTokenService.getTokenUsages(
                page,
                limit,
                order,
                dir,
                keywords);
    }

}
