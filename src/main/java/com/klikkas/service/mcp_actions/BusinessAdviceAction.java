package com.klikkas.service.mcp_actions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.IntentDetectionResponse.IntentParamValue;
import com.klikkas.dto.ai.McpActionResponse;
import com.klikkas.dto.ai.OpenAiResponse;
import com.klikkas.security.TenantContext;
import com.klikkas.service.OpenAiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessAdviceAction {

    private final OpenAiService openAiService;

    private static final String SYSTEM = """
            Kamu adalah konsultan bisnis UMKM yang ramah dan praktis.
            Berikan saran bisnis dalam Bahasa Indonesia, 3-5 kalimat pendek.
            Fokus pada langkah konkret yang bisa langsung dilakukan.
            Gunakan emoji secukupnya. Jangan mengarang data yang tidak ada.""";

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID userID = TenantContext.getUserId();

        String topic = paramStr(params, "topic");
        String userPrompt = (topic != null && !topic.isBlank())
                ? "Saya butuh saran bisnis tentang: " + topic
                : "Beri saya saran bisnis umum untuk usaha kecil-menengah.";

        long t1 = System.nanoTime();
        OpenAiResponse resp = openAiService.chat(userID, "BusinessAdvice", SYSTEM, userPrompt);

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("llm_call", ms(t1));

        String message = (resp.body() != null && !resp.body().isBlank())
                ? resp.body()
                : "Maaf, saya belum bisa memberikan saran saat ini. Coba tanya lagi ya! 🤔";

        return new McpActionResponse(message, pd, List.of());
    }

    // ── helpers ────────────────────────────────────────────────────

    private static String paramStr(Map<String, IntentParamValue> p, String key) {
        IntentParamValue v = p.get(key);
        return (v != null && !v.values().isEmpty()) ? v.values().get(0) : null;
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}