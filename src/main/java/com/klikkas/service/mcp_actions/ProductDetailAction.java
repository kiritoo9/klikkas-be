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
public class ProductDetailAction {

    @PersistenceContext
    private EntityManager em;

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID tenantID = TenantContext.getTenantId();
        String productIdStr = paramStr(params, "product_id");
        String sku = paramStr(params, "sku");

        Tuple row = null;

        if (productIdStr != null) {
            try {
                var q1 = em.createNativeQuery(
                        "SELECT p.*, pc.name AS category_name FROM products p "
                        + "LEFT JOIN product_categories pc ON pc.id = p.category_id "
                        + "WHERE p.id = :id AND p.deleted_at IS NULL AND p.tenant_id = :tid", Tuple.class)
                        .setParameter("id", UUID.fromString(productIdStr))
                        .setParameter("tid", tenantID);
                @SuppressWarnings("unchecked")
                List<Tuple> result = q1.getResultList();
                row = result.isEmpty() ? null : result.get(0);
            } catch (IllegalArgumentException ignored) {}
        }

        if (row == null && sku != null) {
            var q2 = em.createNativeQuery(
                    "SELECT p.*, pc.name AS category_name FROM products p "
                    + "LEFT JOIN product_categories pc ON pc.id = p.category_id "
                    + "WHERE LOWER(p.sku) = :sku AND p.deleted_at IS NULL AND p.tenant_id = :tid", Tuple.class)
                    .setParameter("sku", sku.toLowerCase())
                    .setParameter("tid", tenantID);
            @SuppressWarnings("unchecked")
            List<Tuple> result = q2.getResultList();
            row = result.isEmpty() ? null : result.get(0);
        }

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("query", ms(t0));

        if (row == null) {
            return new McpActionResponse("Produk tidak ditemukan. 😅", pd, List.of());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.get("id", UUID.class));
        data.put("sku", row.get("sku", String.class));
        data.put("name", row.get("name", String.class));
        data.put("description", row.get("description", String.class));
        data.put("buy_price", row.get("buy_price", Number.class));
        data.put("sell_price", row.get("sell_price", Number.class));
        data.put("stock", row.get("stock", Number.class));
        data.put("is_active", row.get("is_active", Boolean.class));
        data.put("category", row.get("category_name", String.class));
        data.put("created_at", toString(row.get("created_at")));

        String msg = String.format("Produk #%s — %s, stok %d, harga jual Rp%d.",
                row.get("sku", String.class), row.get("name", String.class),
                row.get("stock", Number.class), row.get("sell_price", Number.class));
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