package com.klikkas.dto.ai;

import java.util.List;

public record IntentDefinition(
    String intentCode,
    String description,
    String descriptionId,
    List<IntentParam> params
) {
    public record IntentParam(
        String name,
        String type,
        List<String> operators,
        List<String> allowedValues
    ) {}
}