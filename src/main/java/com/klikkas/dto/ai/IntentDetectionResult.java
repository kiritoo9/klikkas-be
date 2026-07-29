package com.klikkas.dto.ai;

public record IntentDetectionResult(
    String intent,
    Float confidence
) {}