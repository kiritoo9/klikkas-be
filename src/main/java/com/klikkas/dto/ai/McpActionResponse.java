package com.klikkas.dto.ai;

import java.util.List;
import java.util.Map;

public record McpActionResponse(
    String message,
    Map<String, Object> process_detail,
    List<Map<String, Object>> data
) {}
