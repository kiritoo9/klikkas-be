package com.klikkas.service.mcp_actions;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.IntentDetectionResponse.IntentParamValue;
import com.klikkas.dto.ai.McpActionResponse;
import com.klikkas.dto.ai.OpenAiResponse;
import com.klikkas.service.OpenAiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChitChatService {

    private static final String BASE_KNOWLEDGE_PATH = "data/klik_kas_assistant.md";

    private final OpenAiService openAiService;

    private String baseKnowledge;

    private String loadBaseKnowledge() {
        if (baseKnowledge == null) {
            try (InputStream is = new ClassPathResource(BASE_KNOWLEDGE_PATH).getInputStream()) {
                baseKnowledge = new String(is.readAllBytes());
            } catch (Exception e) {
                log.warn("Failed to load base knowledge, using fallback");
                baseKnowledge = "You are Klik Kas Assistant, a friendly AI companion for the Klik Kas cashflow app. Speak Indonesian casually, 1-3 short sentences, use emojis.";
            }
        }
        return baseKnowledge;
    }

    public McpActionResponse handle(UUID userID, String userMessage, Map<String, IntentParamValue> params) {
        Map<String, Object> pd = new LinkedHashMap<>();

        long t0 = System.nanoTime();
        String systemPrompt = loadBaseKnowledge();
        pd.put("prompt", ms(t0));

        long t1 = System.nanoTime();
        OpenAiResponse resp = openAiService.chat(userID, "Chitchat", systemPrompt, userMessage);
        pd.put("llm_call", ms(t1));

        String message = (resp.body() != null && !resp.body().isBlank())
            ? resp.body()
            : "Halo! 😊 Ada yang bisa Klik Kas bantu?";
        return new McpActionResponse(message, pd, List.of());
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}