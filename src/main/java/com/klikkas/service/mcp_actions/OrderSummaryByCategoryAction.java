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
public class OrderSummaryByCategoryAction {

    @PersistenceContext
    private EntityManager em;

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID tenantID = TenantContext.getTenantId();
        String dateFrom = paramStr(params, "date_from");
        String dateTo = paramStr(params, "date_to");
        String orderType = paramStr(params, "order_type");

        LocalDateTime start = dateFrom != null
                ? LocalDate.parse(dateFrom).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = dateTo != null
                ? LocalDate.parse(dateTo).atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        String sql = "SELECT COALESCE(SUM(o.grand_total), 0) AS total, "
                + "o.order_type, c.name AS category "
                + "FROM orders o "
                + "JOIN order_categories c ON c.id = o.category_id "
                + "WHERE o.deleted_at IS NULL "
                + "AND o.tenant_id = :tid "
                + "AND LOWER(o.status) = 'paid' "
                + "AND o.order_date BETWEEN :start AND :end";

        if (orderType != null) sql += " AND LOWER(o.order_type) = :orderType";

        sql += " GROUP BY c.name, o.order_type ORDER BY total DESC";

        var q = em.createNativeQuery(sql, Tuple.class);
        q.setParameter("tid", tenantID);
        q.setParameter("start", start);
        q.setParameter("end", end);
        if (orderType != null) q.setParameter("orderType", orderType.toLowerCase());

        @SuppressWarnings("unchecked")
        List<Tuple> rows = q.getResultList();

        List<Map<String, Object>> data = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", r.get("category", String.class));
            m.put("order_type", r.get("order_type", String.class));
            m.put("total", r.get("total", Number.class));
            return m;
        }).toList();

        long total = data.stream().mapToLong(d -> ((Number) d.get("total")).longValue()).sum();
        String msg = String.format("Ringkasan %d kategori, total Rp%,d.", data.size(), total);

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("query", ms(t0));
        return new McpActionResponse(msg, pd, data);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static String paramStr(Map<String, IntentParamValue> p, String key) {
        IntentParamValue v = p.get(key);
        return (v != null && !v.values().isEmpty()) ? v.values().get(0) : null;
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}