package com.klikkas.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.klikkas.dto.ai.OpenAiResponse;
import com.klikkas.entity.User;
import com.klikkas.entity.UserToken;
import com.klikkas.entity.UserTokenUsages;
import com.klikkas.repository.UserRepository;
import com.klikkas.repository.UserTokenRepository;
import com.klikkas.repository.UserTokenUsageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.url:#{null}}")
    private String openAiUrl;

    @Value("${openai.api-key:#{null}}")
    private String openAiApiKey;

    @Value("${openai.model:#{null}}")
    private String openAiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final UserTokenUsageRepository userTokenUsageRepository;

    public OpenAiResponse chat(
            UUID userID,
            String title,
            String systemPrompt,
            String userPrompt) {
        // check available user first
        Optional<User> user = userRepository.findByIdAndDeletedAtIsNull(userID);
        if (!user.isPresent()) {
            log.error("User not found");
            return new OpenAiResponse(null, null);
        }

        UserToken userToken = userTokenRepository.findByUserId(userID);
        if (userToken == null) {
            log.error("User have no token");
            return new OpenAiResponse(null, null);
        }

        // perform to call LLM
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "max_tokens", 4096,
                "temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        long startTime = System.currentTimeMillis();
        log.info("=== OpenAI Request Started ===");
        log.info("URL: {}", openAiUrl);
        log.info("Model: {}", openAiModel);
        log.info("System prompt length: {} chars", systemPrompt.length());
        log.info("User prompt length: {} chars", userPrompt.length());

        ResponseEntity<Map> response = restTemplate.postForEntity(openAiUrl, entity, Map.class);

        long duration = System.currentTimeMillis() - startTime;

        if (response.getBody() == null) {
            log.error("OpenAI response body is null");
            return new OpenAiResponse(null, null);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> usage = (Map<String, Object>) response.getBody().get("usage");

        if (usage != null) {
            Integer promptTokens = (Integer) usage.get("prompt_tokens");
            Integer completionTokens = (Integer) usage.get("completion_tokens");
            Integer totalTokens = (Integer) usage.get("total_tokens");

            log.info("=== OpenAI Response Received ===");
            log.info("Prompt tokens: {}", promptTokens);
            log.info("Completion tokens: {}", completionTokens);
            log.info("Total tokens: {}", totalTokens);
            log.info("Response time: {}ms", duration);
            log.info("Model: {}", response.getBody().get("model"));
            log.info("Finish reason: {}", response.getBody().get("finish_reason"));

            ObjectMapper objectMapper = new ObjectMapper();

            // record user token and usage here
            UserTokenUsages tokenUsage = new UserTokenUsages();
            user.ifPresent(tokenUsage::setUser);
            tokenUsage.setUsage_title(title);
            tokenUsage.setLlm_model(openAiModel);
            tokenUsage.setInput_token(promptTokens);
            tokenUsage.setOutput_token(completionTokens);
            tokenUsage.setResponse_body(
                    objectMapper.writeValueAsString(response.getBody()));
            tokenUsage.setCreatedAt(LocalDateTime.now());
            tokenUsage = userTokenUsageRepository.save(tokenUsage);

            // update user token summary
            userToken.setUsage_token(userToken.getUsage_token() + totalTokens);
            userToken.setUpdated_at(LocalDateTime.now());
            userToken = userTokenRepository.save(userToken);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        String content = null;
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            content = (String) message.get("content");
        }

        log.debug("Response content: {}", content);

        // strip ALL markdown fences (```json, ```, ~~~json, ~~~, etc.) from both ends
        if (content != null) {
            content = content.trim();
            // strip from start
            while (content.startsWith("`") || content.startsWith("~")) {
                if (content.startsWith("```json") || content.startsWith("~~~")) {
                    content = content.substring(7);
                } else if (content.startsWith("```") || content.startsWith("~~~")) {
                    content = content.substring(3);
                } else {
                    content = content.substring(1);
                }
            }
            // strip from end
            while (content.endsWith("`") || content.endsWith("~")) {
                if (content.endsWith("```") || content.endsWith("~~~")) {
                    content = content.substring(0, content.length() - 3);
                } else {
                    content = content.substring(0, content.length() - 1);
                }
            }
            content = content.trim();
        }

        return new OpenAiResponse(null, content);
    }

}
