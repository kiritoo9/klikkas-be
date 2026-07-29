package com.klikkas.service.mcp_actions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
public class OrderListAction {

    @PersistenceContext
    private EntityManager em;

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID tenantID = TenantContext.getTenantId();

        int limit = paramInt(params, "limit", 10);
        String orderType = paramStr(params, "order_type");
        String status = paramStr(params, "status");
        String keywords = paramStr(params, "keywords");
        String categoryName = paramStr(params, "category_name");
        String dateFrom = paramStr(params, "date_from");
        String dateTo = paramStr(params, "date_to");

        // ── build SQL ──────────────────────────────────────────────────────
        String select = "SELECT o.id, o.no_order, o.order_type, o.order_date, "
                + "o.grand_total, o.status, o.remark, o.total_qty, o.total_price, "
                + "o.tax_amount, o.discount_amount, c.name AS category_name";
        String from = " FROM orders o LEFT JOIN order_categories c ON c.id = o.category_id";
        List<String> where = new ArrayList<>();
        where.add("o.deleted_at IS NULL");
        where.add("o.tenant_id = :tenantID");

        if (orderType != null)
            where.add("LOWER(o.order_type) = :orderType");
        if (status != null)
            where.add("LOWER(o.status) = :status");
        if (categoryName != null)
            where.add("LOWER(c.name) = :categoryName");
        if (keywords != null) {
            String like = "'%" + keywords.toLowerCase().replace("'", "''") + "%'";
            where.add("(LOWER(o.no_order) LIKE " + like + " OR LOWER(o.remark) LIKE " + like + ")");
        }
        if (dateFrom != null)
            where.add("o.order_date >= :dateFrom");
        if (dateTo != null)
            where.add("o.order_date <= :dateTo");

        String sql = select + from + " WHERE " + String.join(" AND ", where)
                + " ORDER BY o.created_at DESC LIMIT :limit";

        var q = em.createNativeQuery(sql, Tuple.class);
        q.setParameter("tenantID", tenantID);
        q.setParameter("limit", limit);
        if (orderType != null)
            q.setParameter("orderType", orderType.toLowerCase());
        if (status != null)
            q.setParameter("status", status.toLowerCase());
        if (categoryName != null)
            q.setParameter("categoryName", categoryName.toLowerCase());
        if (dateFrom != null)
            q.setParameter("dateFrom", LocalDate.parse(dateFrom).atStartOfDay());
        if (dateTo != null)
            q.setParameter("dateTo", LocalDate.parse(dateTo).atTime(LocalTime.MAX));

        @SuppressWarnings("unchecked")
        List<Tuple> rows = q.getResultList();

        List<Map<String, Object>> data = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.get("id", UUID.class));
            m.put("no_order", r.get("no_order", String.class));
            m.put("order_type", r.get("order_type", String.class));
            m.put("order_date", toString(r.get("order_date")));
            m.put("grand_total", r.get("grand_total", Number.class));
            m.put("status", r.get("status", String.class));
            m.put("remark", r.get("remark", String.class));
            m.put("category", r.get("category_name", String.class));
            return m;
        }).toList();

        String msg = String.format("Ditemukan %d pesanan.", data.size());

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("query", ms(t0));
        return new McpActionResponse(msg, pd, data);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static int paramInt(Map<String, IntentParamValue> p, String key, int fallback) {
        IntentParamValue v = p.get(key);
        if (v == null || v.values().isEmpty())
            return fallback;
        try {
            return Integer.parseInt(v.values().get(0));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String paramStr(Map<String, IntentParamValue> p, String key) {
        IntentParamValue v = p.get(key);
        return (v != null && !v.values().isEmpty()) ? v.values().get(0) : null;
    }

    private static String ms(long startNanos) {
        return String.format("%.1fms", (System.nanoTime() - startNanos) / 1_000_000.0);
    }

    private static String toString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime) {
            return obj.toString();
        }
        if (obj instanceof java.sql.Timestamp) {
            return obj.toString();
        }
        return obj.toString();
    }
}