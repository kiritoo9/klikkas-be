package com.klikkas.service.mcp_actions;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class JournalReportAction {

    @PersistenceContext
    private EntityManager em;

    public McpActionResponse execute(Map<String, IntentParamValue> params) {
        long t0 = System.nanoTime();
        UUID tenantID = TenantContext.getTenantId();

        String dateFrom = paramStr(params, "date_from");
        String dateTo = paramStr(params, "date_to");
        String accountCode = paramStr(params, "account_code");
        String accountName = paramStr(params, "account_name");

        LocalDateTime start = dateFrom != null
                ? LocalDate.parse(dateFrom).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = dateTo != null
                ? LocalDate.parse(dateTo).atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        // ── build SQL ──────────────────────────────────────────────
        String select = "SELECT j.id AS journal_id, j.journal_date, j.description, "
                + "a.code AS account_code, a.name AS account_name, a.type AS account_type, "
                + "jd.debit, jd.credit, jd.remark";
        String from = " FROM journals j "
                + "JOIN journal_details jd ON jd.journal_id = j.id "
                + "JOIN accounts a ON a.id = jd.account_id";
        List<String> where = new ArrayList<>();
        where.add("j.deleted_at IS NULL");
        where.add("jd.deleted_at IS NULL");
        where.add("j.journal_date BETWEEN :start AND :end");

        if (accountCode != null) where.add("LOWER(a.code) = :accountCode");
        if (accountName != null) where.add("LOWER(a.name) LIKE :accountName");

        String sql = select + from + " WHERE " + String.join(" AND ", where)
                + " ORDER BY j.journal_date DESC, a.code ASC LIMIT 100";

        var q = em.createNativeQuery(sql, Tuple.class);
        q.setParameter("start", start);
        q.setParameter("end", end);
        if (accountCode != null) q.setParameter("accountCode", accountCode.toLowerCase());
        if (accountName != null) q.setParameter("accountName", "%" + accountName.toLowerCase() + "%");

        @SuppressWarnings("unchecked")
        List<Tuple> rows = q.getResultList();

        List<Map<String, Object>> data = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("journal_id", r.get("journal_id", UUID.class));
            m.put("journal_date", toString(r.get("journal_date")));
            m.put("description", r.get("description", String.class));
            m.put("account_code", r.get("account_code", String.class));
            m.put("account_name", r.get("account_name", String.class));
            m.put("account_type", r.get("account_type", String.class));
            m.put("debit", r.get("debit", Number.class));
            m.put("credit", r.get("credit", Number.class));
            m.put("remark", r.get("remark", String.class));
            return m;
        }).toList();

        long totalDebit = data.stream().mapToLong(d -> ((Number) d.get("debit")).longValue()).sum();
        long totalCredit = data.stream().mapToLong(d -> ((Number) d.get("credit")).longValue()).sum();

        String msg = String.format("Ditemukan %d entri jurnal. Total debit Rp%,d, kredit Rp%,d.",
                data.size(), totalDebit, totalCredit);

        Map<String, Object> pd = new LinkedHashMap<>();
        pd.put("query", ms(t0));
        return new McpActionResponse(msg, pd, data);
    }

    // ── helpers ────────────────────────────────────────────────────

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