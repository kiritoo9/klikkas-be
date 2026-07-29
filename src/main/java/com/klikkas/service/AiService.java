package com.klikkas.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.FinanceHealth;
import com.klikkas.dto.ai.OpenAiResponse;
import com.klikkas.dto.ai.GenReportRequest;
import com.klikkas.dto.ai.GenReportResponse;
import com.klikkas.dto.ai.McpResponse;
import com.klikkas.dto.cashflows.LabaRugiResponse;
import com.klikkas.dto.cashflows.SumByCategory;
import com.klikkas.entity.UserToken;
import com.klikkas.repository.UserTokenRepository;
import com.klikkas.security.TenantContext;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AiService {

    private final UserTokenRepository userTokenRepository;
    private final McpService mcpService;

    private final CashFlowService cashFlowService;
    private final OpenAiService openAiService;

    public Map<String, String> financeHealthPrompt(String payloadBody) {
        Map<String, String> data = new HashMap<>();

        // system prompt
        data.put("system", """
            You are a financial analysis expert.
            Analyze the provided financial data and return the business financial health.

            Return JSON only:
            {
            "score": 0,
            "summary_message": "",
            "suggest_message": "",
            "solution_message": ""
            }

            score must be 0-100.
            Do not invent data.
            Return all messages in Indonesian (id-ID) with a casual and warm tone.
            Keep messages concise and actionable.
        """);
        data.put("user", payloadBody);

        return data;
    }

    public FinanceHealth checkFinanceHealth() {
        UUID userID = TenantContext.getUserId();

        // check user token available
        UserToken userToken = userTokenRepository.findByUserId(userID);
        if (userToken != null && userToken.getUsage_token() < userToken.getLimit_token()) {
            ObjectMapper objectMapper = new ObjectMapper();
            LocalDate dateFrom = LocalDate.now().minusMonths(1);
            LocalDate dateTo = LocalDate.now();

            // get report laba rugi
            LabaRugiResponse labaRugi = cashFlowService.getLabaRugi(dateFrom, dateTo);

            // get sum order by category
            List<SumByCategory> dataSum = cashFlowService.getSumOrderByCategory(dateFrom, dateTo);

            // prepare summarize data
            Map<String, Object> sumData = new HashMap<>();
            sumData.put("laporan_laba_rugi", objectMapper.writeValueAsString(labaRugi));
            sumData.put("summary_order_by_category", objectMapper.writeValueAsString(dataSum));
    
            // call LLM for analyzing
            String payloadBody = objectMapper.writeValueAsString(sumData);
            Map<String, String> prompts = financeHealthPrompt(payloadBody);

            OpenAiResponse aiResponse = openAiService.chat(
                userID, 
                "Finance Health Check",
                prompts.get("system"), 
                prompts.get("user")
            );

            if (aiResponse.body() != null && !aiResponse.body().isEmpty()) {
                try {
                    FinanceHealth output = objectMapper.readValue(aiResponse.body(), FinanceHealth.class);
                    
                    return new FinanceHealth(
                        true,
                        output.score(),
                        output.summary_message(),
                        output.suggest_message(),
                        output.solution_message()
                    );
                } catch (Exception e) {
                    System.err.println(e);
                    // log.warn("Failed to parse FinanceHealth response: {}. Raw content: {}", e.getMessage(), aiResponse.body());
                }
            }
        }

        // return data
        return new FinanceHealth(
            false,
            0,
            "",
            "",
            ""
        );
    }
    
    public GenReportResponse generativeReport(GenReportRequest req) {
        UUID userID = TenantContext.getUserId();
        UUID tenantID = TenantContext.getTenantId();

        McpResponse response = mcpService.run(
            userID,
            tenantID,
            req.content()
        );

        return new GenReportResponse(
            true,
            "Success",
            response.ai_response(),
            response.process_detail(),
            response.params(),
            response.data()
        );
    }
}
