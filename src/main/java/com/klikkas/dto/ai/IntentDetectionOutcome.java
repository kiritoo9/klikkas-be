package com.klikkas.dto.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public record IntentDetectionOutcome(
    IntentDetectionResponse response,
    Map<String, String> timings
) {
    public IntentDetectionOutcome(IntentDetectionResponse response) {
        this(response, new LinkedHashMap<>());
    }
}