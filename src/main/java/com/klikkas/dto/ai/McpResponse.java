package com.klikkas.dto.ai;

import java.util.List;
import java.util.Map;

public record McpResponse(
    String ai_response,
    Map<String, Object> process_detail,
    List<Map<String, Object>> params,
    List<Map<String, Object>> data
) {}