package com.klikkas.service.mcp_actions;

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
public class ProductListAction {

    @PersistenceContext
    private EntityManager em;

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID tenantID = TenantContext.getTenantId();

        int limit = paramInt(params, "limit", 10);
        String productName = paramStr(params, "product_name");
        String sku = paramStr(params, "sku");
        String categoryName = paramStr(params, "category_name");
        String isActive = paramStr(params, "is_active");

        // ── build SQL ──────────────────────────────────────────────────────
        String select = "SELECT p.id, p.sku, p.name, p.description, p.buy_price, "
                + "p.sell_price, p.stock, p.is_active, p.created_at, "
                + "pc.name AS category_name";
        String from = " FROM products p LEFT JOIN product_categories pc ON pc.id = p.category_id";
        List<String> where = new ArrayList<>();
        where.add("p.deleted_at IS NULL");
        where.add("p.tenant_id = :tenantID");

        if (productName != null) {
            where.add("LOWER(p.name) LIKE :productName");
        }
        if (sku != null) {
            where.add("LOWER(p.sku) LIKE :sku");
        }
        if (categoryName != null) {
            where.add("LOWER(pc.name) = :categoryName");
        }
        if (isActive != null) {
            where.add("p.is_active = :isActive");
        }

        String sql = select + from + " WHERE " + String.join(" AND ", where)
                + " ORDER BY p.created_at DESC LIMIT :limit";

        var q = em.createNativeQuery(sql, Tuple.class);
        q.setParameter("tenantID", tenantID);
        q.setParameter("limit", limit);
        if (productName != null)
            q.setParameter("productName", "%" + productName.toLowerCase().replace("'", "''") + "%");
        if (sku != null)
            q.setParameter("sku", "%" + sku.toLowerCase().replace("'", "''") + "%");
        if (categoryName != null)
            q.setParameter("categoryName", categoryName.toLowerCase().replace("'", "''"));
        if (isActive != null)
            q.setParameter("isActive", Boolean.parseBoolean(isActive));

        @SuppressWarnings("unchecked")
        List<Tuple> rows = q.getResultList();

        List<Map<String, Object>> data = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.get("id", UUID.class));
            m.put("sku", r.get("sku", String.class));
            m.put("name", r.get("name", String.class));
            m.put("description", r.get("description", String.class));
            m.put("buy_price", r.get("buy_price", Number.class));
            m.put("sell_price", r.get("sell_price", Number.class));
            m.put("stock", r.get("stock", Number.class));
            m.put("is_active", r.get("is_active", Boolean.class));
            m.put("category", r.get("category_name", String.class));
            m.put("created_at", toString(r.get("created_at")));
            return m;
        }).toList();

        String msg = String.format("Ditemukan %d produk.", data.size());

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
        if (obj instanceof java.time.LocalDateTime) return obj.toString();
        if (obj instanceof java.sql.Timestamp) return obj.toString();
        return obj.toString();
    }
}
