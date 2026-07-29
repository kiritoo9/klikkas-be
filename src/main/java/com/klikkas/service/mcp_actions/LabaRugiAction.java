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
public class LabaRugiAction {

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

        // ── 1. top-level totals by order_type ──────────────────────────────
        var q = em.createNativeQuery(
                "SELECT o.order_type, SUM(o.grand_total) AS total, COUNT(*) AS count "
                + "FROM orders o "
                + "WHERE o.deleted_at IS NULL AND o.tenant_id = :tid "
                + "AND LOWER(o.status) = 'paid' "
                + "AND o.order_date BETWEEN :start AND :end "
                + "GROUP BY o.order_type", Tuple.class);
        q.setParameter("tid", tenantID);
        q.setParameter("start", start);
        q.setParameter("end", end);

        @SuppressWarnings("unchecked")
        List<Tuple> rows = q.getResultList();

        long pendapatan = 0, beban = 0;
        int countPendapatan = 0, countBeban = 0;
        for (Tuple r : rows) {
            String type = r.get("order_type", String.class);
            long total = r.get("total", Number.class).longValue();
            int cnt = r.get("count", Number.class).intValue();
            if ("kas_masuk".equalsIgnoreCase(type)) {
                pendapatan = total;
                countPendapatan = cnt;
            } else if ("kas_keluar".equalsIgnoreCase(type)) {
                beban = total;
                countBeban = cnt;
            }
        }

        long labaRugi = pendapatan - beban;

        // ── 2. detail per category ──────────────────────────────────────────
        var q2 = em.createNativeQuery(
                "SELECT c.name AS category, o.order_type, SUM(o.grand_total) AS total, COUNT(*) AS count "
                + "FROM orders o JOIN order_categories c ON c.id = o.category_id "
                + "WHERE o.deleted_at IS NULL AND o.tenant_id = :tid "
                + "AND LOWER(o.status) = 'paid' "
                + "AND o.order_date BETWEEN :start AND :end "
                + "GROUP BY c.name, o.order_type ORDER BY total DESC", Tuple.class);
        q2.setParameter("tid", tenantID);
        q2.setParameter("start", start);
        q2.setParameter("end", end);

        @SuppressWarnings("unchecked")
        List<Tuple> detailRows = q2.getResultList();

        List<Map<String, Object>> detailData = detailRows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", r.get("category", String.class));
            m.put("order_type", r.get("order_type", String.class));
            m.put("total", r.get("total", Number.class));
            m.put("count", r.get("count", Number.class));
            return m;
        }).toList();

        // ── 3. combine into response ────────────────────────────────────────
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("periode", start.toLocalDate() + " — " + end.toLocalDate());
        summary.put("pendapatan", pendapatan);
        summary.put("jumlah_pendapatan", countPendapatan);
        summary.put("beban", beban);
        summary.put("jumlah_beban", countBeban);
        summary.put("laba_rugi", labaRugi);

        String status = labaRugi >= 0 ? "laba" : "rugi";
        String msg = String.format(
                "Periode %s — %s Rp%,d. Pendapatan Rp%,d (%d transaksi), beban Rp%,d (%d transaksi).",
                summary.get("periode"), status, Math.abs(labaRugi),
                pendapatan, countPendapatan, beban, countBeban);

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("query", ms(t0));

        // put summary first, then detail rows
        java.util.List<Map<String, Object>> all = new java.util.ArrayList<>();
        all.add(summary);
        all.addAll(detailData);

        return new McpActionResponse(msg, pd, all);
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