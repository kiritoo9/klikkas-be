package com.klikkas.dto.ai;

import java.util.List;
import java.util.Map;

public record GenReportResponse(
    Boolean success,
    String message,
    String ai_response,
    Map<String, Object> process_detail,
    List<Map<String, Object>> params,
    List<Map<String, Object>> data
) {}
