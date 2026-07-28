package com.klikkas.dto.ai;

public record OpenAiResponse(
        Float confidence,
        String body) {
}