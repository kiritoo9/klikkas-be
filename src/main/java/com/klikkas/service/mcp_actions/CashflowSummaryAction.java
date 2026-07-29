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
public class CashflowSummaryAction {

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

        // ── kas masuk vs kas keluar ────────────────────────────────────────
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

        long kasMasuk = 0, kasKeluar = 0;
        int countMasuk = 0, countKeluar = 0;

        for (Tuple r : rows) {
            String type = r.get("order_type", String.class);
            long total = r.get("total", Number.class).longValue();
            int cnt = r.get("count", Number.class).intValue();
            if ("kas_masuk".equalsIgnoreCase(type)) {
                kasMasuk = total;
                countMasuk = cnt;
            } else if ("kas_keluar".equalsIgnoreCase(type)) {
                kasKeluar = total;
                countKeluar = cnt;
            }
        }

        long netFlow = kasMasuk - kasKeluar;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("periode", start.toLocalDate() + " — " + end.toLocalDate());
        data.put("kas_masuk", kasMasuk);
        data.put("jumlah_transaksi_masuk", countMasuk);
        data.put("kas_keluar", kasKeluar);
        data.put("jumlah_transaksi_keluar", countKeluar);
        data.put("arus_kas_bersih", netFlow);

        String direction = netFlow >= 0 ? "surplus" : "defisit";
        String msg = String.format(
                "Arus kas %s — %s. Pemasukan Rp%,d (%d transaksi), pengeluaran Rp%,d (%d transaksi).",
                direction, data.get("periode"), kasMasuk, countMasuk, kasKeluar, countKeluar);

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("query", ms(t0));
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
}