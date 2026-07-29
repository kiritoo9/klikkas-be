package com.klikkas.dto.ai;

import java.util.List;
import java.util.Map;

public record IntentDetectionResponse(
    String intent,
    Float confidence,
    Map<String, IntentParamValue> params
) {
    public record IntentParamValue(
        List<String> values,
        String operator
    ) {}
}