package com.klikkas.service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.IntentDefinition;
import com.klikkas.dto.ai.IntentDetectionOutcome;
import com.klikkas.dto.ai.IntentDetectionResponse;
import com.klikkas.dto.ai.IntentDetectionResponse.IntentParamValue;
import com.klikkas.dto.ai.IntentDetectionResult;
import com.klikkas.dto.ai.OpenAiResponse;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class IntentService {

    private static final float CONFIDENCE_THRESHOLD = 0.4f;
    private static final String INTENTS_PATH = "data/intents.json";

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<IntentDefinition> intentContext;

    // ── Load context ──────────────────────────────────────────────────────────

    private List<IntentDefinition> loadIntentContext() {
        if (intentContext == null) {
            try (InputStream is = new ClassPathResource(INTENTS_PATH).getInputStream()) {
                intentContext = objectMapper.readValue(is, new TypeReference<List<IntentDefinition>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Failed to load intents from " + INTENTS_PATH, e);
            }
        }
        return intentContext;
    }

    private IntentDefinition findIntent(String intentCode) {
        return loadIntentContext().stream()
            .filter(i -> i.intentCode().equals(intentCode))
            .findFirst()
            .orElse(null);
    }

    // ── Step 1: detect intent (slim prompt — only intentCode + descriptionId) ─

    private String buildIntentPrompt(String intentsJson) {
        return """
            You are an intent classifier for a cashflow application.
            Match the user's message to the closest intent below.
            Return ONLY valid JSON — no markdown fences, no extra text.

            Available intents:
            %s

            Format:
            {"intent": "intent_code", "confidence": 0.0_to_1.0}

            If no intent matches well, use "unknown" with confidence < 0.4.
            All messages are in Indonesian. "pesanan" = orders, "produk" = products,
            "laba rugi" = profit/loss, "arus kas" = cashflow, "jurnal" = journal,
            "saran" / "rekomendasi" / "ekspansi" = business_advice.
            """.formatted(intentsJson);
    }

    public IntentDetectionResult detectIntent(UUID userID, String content) {
        List<IntentDefinition> intents = loadIntentContext();

        List<Map<String, String>> slim = intents.stream()
            .map(i -> Map.of("intentCode", i.intentCode(), "descriptionId", i.descriptionId()))
            .toList();

        String intentsJson = objectMapper.writeValueAsString(slim);
        String systemPrompt = buildIntentPrompt(intentsJson);

        OpenAiResponse aiResponse = openAiService.chat(
            userID, "Intent Detection", systemPrompt, content);

        String raw = aiResponse.body();
        if (raw == null || raw.isEmpty()) {
            return new IntentDetectionResult("unknown", 0.0f);
        }
        return objectMapper.readValue(raw, IntentDetectionResult.class);
    }

    // ── Step 2: extract params (scoped to matched intent) ─────────────────────

    private String buildParamsPrompt(String intentJson, String intentCode, String todayDate) {
        return """
            You are a parameter extractor for a cashflow application.
            Extract parameters from the user's message based on the intent definition below.
            Return ONLY valid JSON — no markdown fences, no extra text.

            Intent: %s
            Definition: %s

            Rules:
            1. Extract parameter values from the user's message.
            2. Use the operator that best matches how the user expressed the filter.
            3. If the user mentions a relative date (e.g. "bulan ini", "minggu lalu", "hari ini"),
               resolve it to actual date values (YYYY-MM-DD format).
               - Today's date is: %s
               - "bulan ini" = first day of current month to today.
               - "bulan lalu" = previous full month.
               - "minggu ini" = Monday of current week to today.
               - "minggu lalu" = Monday to Sunday of previous week.
               - "hari ini" = today's date.
            4. If a parameter is not mentioned, omit it from the result.

            Format:
            {
                "params": {
                    "param_name": {
                        "values": ["value1"],
                        "operator": "equal|contains|range_date|gte|lte"
                    }
                }
            }
            """.formatted(intentCode, intentJson, todayDate);
    }

    @SuppressWarnings("unchecked")
    public Map<String, IntentParamValue> detectParams(
            UUID userID, String content, IntentDefinition intent) {

        if (intent.params().isEmpty()) {
            return Map.of();
        }

        String intentJson = objectMapper.writeValueAsString(intent);
        String todayDate = LocalDate.now().toString();
        String systemPrompt = buildParamsPrompt(intentJson, intent.intentCode(), todayDate);

        OpenAiResponse aiResponse = openAiService.chat(
            userID, "Parameter Extraction: " + intent.intentCode(), systemPrompt, content);

        String raw = aiResponse.body();
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> wrapper = objectMapper.readValue(raw, Map.class);
        Map<String, Map<String, Object>> rawParams = (Map<String, Map<String, Object>>) wrapper.get("params");
        if (rawParams == null) {
            return Map.of();
        }

        Map<String, IntentParamValue> params = new HashMap<>();
        for (var entry : rawParams.entrySet()) {
            Map<String, Object> v = entry.getValue();
            List<String> values = (List<String>) v.get("values");
            String operator = (String) v.get("operator");
            params.put(entry.getKey(), new IntentParamValue(values, operator));
        }
        return params;
    }

    // ── Combined: full 2-step detection ───────────────────────────────────────

    public IntentDetectionOutcome detect(UUID userID, String content) {
        Map<String, String> timings = new java.util.LinkedHashMap<>();

        // Step 1: classify intent
        long t0 = System.nanoTime();
        IntentDetectionResult result = detectIntent(userID, content);
        timings.put("intent_detection", ms(t0));

        if (result.confidence() < CONFIDENCE_THRESHOLD || "unknown".equals(result.intent())) {
            return new IntentDetectionOutcome(
                new IntentDetectionResponse("unknown", result.confidence(), Map.of()),
                timings);
        }

        // Step 2: extract params for matched intent only
        IntentDefinition intent = findIntent(result.intent());
        if (intent == null) {
            return new IntentDetectionOutcome(
                new IntentDetectionResponse("unknown", result.confidence(), Map.of()),
                timings);
        }

        long t1 = System.nanoTime();
        Map<String, IntentParamValue> params = detectParams(userID, content, intent);
        timings.put("param_extraction", ms(t1));

        return new IntentDetectionOutcome(
            new IntentDetectionResponse(result.intent(), result.confidence(), params),
            timings);
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}