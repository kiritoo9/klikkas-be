package com.klikkas.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record GenReportRequest(

    @NotBlank(message = "Content is required")
    String content,

    String follow_up
) {}
