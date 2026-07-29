package com.klikkas.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.IntentDetectionOutcome;
import com.klikkas.dto.ai.IntentDetectionResponse;
import com.klikkas.dto.ai.McpActionResponse;
import com.klikkas.dto.ai.McpResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class McpService {

    private final IntentService intentService;
    private final McpActionService mcpActionService;

    public McpResponse run(UUID userID, UUID tenantID, String content) {
        Map<String, Object> processDetail = new java.util.LinkedHashMap<>();
        long t0 = System.nanoTime();

        try {
            IntentDetectionOutcome outcome = intentService.detect(userID, content);
            // merge intent service timings into processDetail
            outcome.timings().forEach(processDetail::put);

            IntentDetectionResponse detected = outcome.response();

            // convert params Map<String, IntentParamValue> → List<Map<String, Object>>
            List<Map<String, Object>> paramsList = detected.params().entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("values", e.getValue().values());
                    m.put("operator", e.getValue().operator());
                    return m;
                })
                .toList();

            // execute action mapping (intent → handler)
            McpActionResponse actionResp = mcpActionService.execute(
                detected.intent(), detected.params(), userID, tenantID, content);
            // merge action process_detail into main processDetail
            actionResp.process_detail().forEach(processDetail::put);

            // prepare response
            long tr = System.nanoTime();
            processDetail.put("prepare_response", ms(tr));

            processDetail.put("total", ms(t0));

            return new McpResponse(actionResp.message(), processDetail, paramsList, actionResp.data());
        } catch (Exception e) {
            processDetail.put("error", e.getMessage());
            processDetail.put("total", ms(t0));
            return new McpResponse(
                "Waduh, lagi ada gangguan nih. Coba lagi ya nanti 🙏",
                processDetail,
                List.of(),
                List.of()
            );
        }
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}
