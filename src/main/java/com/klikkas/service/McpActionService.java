package com.klikkas.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.IntentDetectionResponse.IntentParamValue;
import com.klikkas.dto.ai.McpActionResponse;
import com.klikkas.security.TenantContext;
import com.klikkas.service.mcp_actions.BusinessAdviceAction;
import com.klikkas.service.mcp_actions.CashflowSummaryAction;
import com.klikkas.service.mcp_actions.ChitChatService;
import com.klikkas.service.mcp_actions.CleaningService;
import com.klikkas.service.mcp_actions.FinanceHealthAction;
import com.klikkas.service.mcp_actions.JournalReportAction;
import com.klikkas.service.mcp_actions.LabaRugiAction;
import com.klikkas.service.mcp_actions.OrderDetailAction;
import com.klikkas.service.mcp_actions.OrderListAction;
import com.klikkas.service.mcp_actions.OrderSummaryByCategoryAction;
import com.klikkas.service.mcp_actions.ProductDetailAction;
import com.klikkas.service.mcp_actions.ProductListAction;
import com.klikkas.service.mcp_actions.TopCategoryAction;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class McpActionService {

    private final ChitChatService chitChatService;
    private final OrderListAction orderListAction;
    private final OrderDetailAction orderDetailAction;
    private final OrderSummaryByCategoryAction orderSummaryByCategoryAction;
    private final ProductListAction productListAction;
    private final ProductDetailAction productDetailAction;
    private final BusinessAdviceAction businessAdviceAction;
    private final JournalReportAction journalReportAction;
    private final FinanceHealthAction financeHealthAction;
    private final CashflowSummaryAction cashflowSummaryAction;
    private final LabaRugiAction labaRugiAction;
    private final TopCategoryAction topCategoryAction;
    private final CleaningService cleaningService;

    public McpActionResponse execute(String intent, Map<String, IntentParamValue> params,
                                     UUID userID, UUID tenantID, String userMessage) {
        TenantContext.setContext(userID != null ? userID.toString() : "", tenantID, userID);
        long t0 = System.nanoTime();

        try {
            McpActionResponse result = switch (intent) {
                case "order_list"                -> orderListAction.execute(params);
                case "order_detail"              -> orderDetailAction.execute(params);
                case "order_summary_by_category" -> orderSummaryByCategoryAction.execute(params);
                case "product_list"              -> productListAction.execute(params);
                case "product_detail"            -> productDetailAction.execute(params);
                case "cashflow_summary"          -> cashflowSummaryAction.execute(params);
                case "laba_rugi"                 -> labaRugiAction.execute(params);
                case "top_category"              -> topCategoryAction.execute(params);
                case "business_advice"           -> businessAdviceAction.execute(params);
                case "journal_report"            -> journalReportAction.execute(params);
                case "finance_health"            -> financeHealthAction.execute(params);
                case "chitchat"                  -> chitChatService.handle(userID, userMessage, params);
                default                          -> placeholder("Intent not recognized: " + intent);
            };

            // clean response to natural language (skip chitchat & empty data)
            if (!"chitchat".equals(intent) && !result.data().isEmpty()) {
                long tClean = System.nanoTime();
                McpActionResponse cleaned = cleaningService.clean(userID, intent, result.data());
                result = new McpActionResponse(cleaned.message(), result.process_detail(), result.data());
                result.process_detail().put("cleaning", ms(tClean));
            }

            result.process_detail().put("action_mapping", ms(t0));
            return result;
        } catch (Exception e) {
            Map<String, Object> pd = new LinkedHashMap<>();
            pd.put("action_mapping", ms(t0));
            pd.put("error", e.getMessage());
            return new McpActionResponse(
                "Waduh, lagi ada gangguan nih. Coba lagi ya nanti 🙏",
                pd, List.of());
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static McpActionResponse placeholder(String msg) {
        return new McpActionResponse(msg, new LinkedHashMap<>(), List.of());
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}