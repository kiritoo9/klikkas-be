package com.klikkas.service.mcp_actions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.klikkas.dto.ai.IntentDetectionResponse.IntentParamValue;
import com.klikkas.dto.ai.McpActionResponse;
import com.klikkas.security.TenantContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;

@Service
public class FinanceHealthAction {

    @PersistenceContext
    private EntityManager em;

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID tenantID = TenantContext.getTenantId();

        String dateFrom = paramStr(params, "date_from");
        String dateTo = paramStr(params, "date_to");

        LocalDateTime start = dateFrom != null
                ? LocalDate.parse(dateFrom).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = dateTo != null
                ? LocalDate.parse(dateTo).atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        // ── 1) Revenue vs Expense ──────────────────────────────────
        String revExpSql = """
                SELECT
                    SUM(CASE WHEN o.order_type = 'income' THEN o.total_amount ELSE 0 END) AS revenue,
                    SUM(CASE WHEN o.order_type = 'expense' THEN o.total_amount ELSE 0 END) AS expense
                FROM orders o
                WHERE o.deleted_at IS NULL
                  AND o.status = 'paid'
                  AND o.order_date BETWEEN :start AND :end
                """;

        Tuple revExp = (Tuple) em.createNativeQuery(revExpSql, Tuple.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        long revenue = ((Number) revExp.get("revenue")).longValue();
        long expense = ((Number) revExp.get("expense")).longValue();
        long profit = revenue - expense;
        double margin = revenue > 0 ? (double) profit / revenue * 100 : 0;

        // ── 2) Top 5 categories by amount ──────────────────────────
        String catSql = """
                SELECT oc.name AS category, o.order_type,
                       SUM(o.total_amount) AS total
                FROM orders o
                JOIN order_categories oc ON oc.id = o.category_id
                WHERE o.deleted_at IS NULL
                  AND o.status = 'paid'
                  AND o.order_date BETWEEN :start AND :end
                GROUP BY oc.name, o.order_type
                ORDER BY total DESC
                LIMIT 5
                """;

        @SuppressWarnings("unchecked")
        List<Tuple> catRows = em.createNativeQuery(catSql, Tuple.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        List<Map<String, Object>> topCategories = catRows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", r.get("category", String.class));
            m.put("order_type", r.get("order_type", String.class));
            m.put("total", r.get("total", Number.class));
            return m;
        }).toList();

        // ── 3) Health status emoji ─────────────────────────────────
        String healthEmoji;
        if (margin >= 20) healthEmoji = "🟢";
        else if (margin >= 5) healthEmoji = "🟡";
        else if (margin >= 0) healthEmoji = "🟠";
        else healthEmoji = "🔴";

        String msg = String.format(
                "%s Kesehatan Keuangan: Pendapatan Rp%,d | Pengeluaran Rp%,d | Laba Rp%,d | Margin %.1f%%",
                healthEmoji, revenue, expense, profit, margin);

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("revenue", revenue);
        pd.put("expense", expense);
        pd.put("profit", profit);
        pd.put("margin_pct", Math.round(margin * 10.0) / 10.0);
        pd.put("health_emoji", healthEmoji);
        pd.put("query", ms(t0));

        return new McpActionResponse(msg, pd, topCategories);
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