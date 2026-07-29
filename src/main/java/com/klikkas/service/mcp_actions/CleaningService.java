package com.klikkas.service.mcp_actions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.McpActionResponse;
import com.klikkas.service.OpenAiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleaningService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM = "Kamu perangkum data bisnis. Jawab singkat, natural, Bahasa Indonesia.";

    public McpActionResponse clean(UUID userID, String intent, List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return new McpActionResponse("Tidak ada data yang ditemukan.", new LinkedHashMap<>(), List.of());
        }

        long t0 = System.nanoTime();
        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to serialize data for cleaning", e);
            return new McpActionResponse("Berikut hasil datanya.", new LinkedHashMap<>(), data);
        }

        String userPrompt = String.format("""
                Hasil query (JSON):
                %s

                Inten: %s

                Jelaskan dalam Bahasa Indonesia, natural, 2-4 kalimat. \
                Rangkum poin utama. Format angka ribuan. \
                Jangan tambahkan info yang tidak ada di data.""",
                json, intent);

        var resp = openAiService.chat(userID, "Cleaning", SYSTEM, userPrompt);

        String message = (resp.body() != null && !resp.body().isBlank())
                ? resp.body()
                : "Berikut ringkasan datanya.";

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("cleaning", ms(t0));
        return new McpActionResponse(message, pd, data);
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}
