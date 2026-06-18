package com.klikkas.dto.accounts;

import java.util.List;

public record AccountListResponse(
    List<AccountResponse> data,
    Integer page,
    Integer totalPage
) {}
