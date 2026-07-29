package com.klikkas.service.mcp_actions;

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
public class OrderDetailAction {

    @PersistenceContext
    private EntityManager em;

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID tenantID = TenantContext.getTenantId();
        String orderIdStr = paramStr(params, "order_id");
        String noOrder = paramStr(params, "no_order");

        Tuple row = null;

        if (orderIdStr != null) {
            try {
                var q1 = em.createNativeQuery(
                        "SELECT o.*, c.name AS category_name FROM orders o "
                        + "LEFT JOIN order_categories c ON c.id = o.category_id "
                        + "WHERE o.id = :id AND o.deleted_at IS NULL AND o.tenant_id = :tid", Tuple.class)
                        .setParameter("id", UUID.fromString(orderIdStr))
                        .setParameter("tid", tenantID);
                @SuppressWarnings("unchecked")
                List<Tuple> result = q1.getResultList();
                row = result.isEmpty() ? null : result.get(0);
            } catch (IllegalArgumentException ignored) {}
        }

        if (row == null && noOrder != null) {
            var q2 = em.createNativeQuery(
                    "SELECT o.*, c.name AS category_name FROM orders o "
                    + "LEFT JOIN order_categories c ON c.id = o.category_id "
                    + "WHERE LOWER(o.no_order) LIKE :no AND o.deleted_at IS NULL AND o.tenant_id = :tid", Tuple.class)
                    .setParameter("no", "%" + noOrder.toLowerCase() + "%")
                    .setParameter("tid", tenantID);
            @SuppressWarnings("unchecked")
            List<Tuple> result = q2.getResultList();
            row = result.isEmpty() ? null : result.get(0);
        }

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("query", ms(t0));

        if (row == null) {
            return new McpActionResponse("Pesanan tidak ditemukan. 😅", pd, List.of());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.get("id", UUID.class));
        data.put("no_order", row.get("no_order", String.class));
        data.put("order_type", row.get("order_type", String.class));
        data.put("order_date", toString(row.get("order_date")));
        data.put("category", row.get("category_name", String.class));
        data.put("total_qty", row.get("total_qty", Number.class));
        data.put("total_price", row.get("total_price", Number.class));
        data.put("tax_amount", row.get("tax_amount", Number.class));
        data.put("discount_amount", row.get("discount_amount", Number.class));
        data.put("grand_total", row.get("grand_total", Number.class));
        data.put("status", row.get("status", String.class));
        data.put("remark", row.get("remark", String.class));

        String msg = String.format("Pesanan #%s — %s, total Rp%,d (%s).",
                data.get("no_order"), data.get("order_type"),
                ((Number) data.get("grand_total")).longValue(), data.get("status"));
        return new McpActionResponse(msg, pd, List.of(data));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static String paramStr(Map<String, IntentParamValue> p, String key) {
        IntentParamValue v = p.get(key);
        return (v != null && !v.values().isEmpty()) ? v.values().get(0) : null;
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }

    private static String toString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime) return obj.toString();
        if (obj instanceof java.sql.Timestamp) return obj.toString();
        return obj.toString();
    }
}