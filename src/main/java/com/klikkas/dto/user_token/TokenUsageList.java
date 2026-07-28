package com.klikkas.dto.user_token;

import java.util.List;

public record TokenUsageList(
    List<TokenUsages> data,
    Integer page,
    Integer total_page
) {}
