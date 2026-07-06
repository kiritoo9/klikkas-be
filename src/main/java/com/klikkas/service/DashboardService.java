package com.klikkas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.klikkas.dto.dashboards.CurrentActivity;
import com.klikkas.dto.dashboards.LatestWeekGraphResponse;
import com.klikkas.dto.dashboards.SummarizeResponse;
import com.klikkas.repository.OrderRepository;
import com.klikkas.security.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepo;

    private Integer calculatePercent(Integer prev, Integer cur) {
        if (prev == 0) {
            return cur == 0 ? 0 : 100;
        }

        return (int) Math.round(
                ((double) (cur - prev) / prev) * 100);
    }

    public SummarizeResponse getSummarize() {
        LocalDate now = LocalDate.now();
        UUID tenantID = TenantContext.getTenantId();

        LocalDateTime currentStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime currentEnd = currentStart.plusMonths(1);

        LocalDateTime prevStart = currentStart.minusMonths(1);

        // get sum data order
        Integer totalKasMasuk = orderRepo.sumDashboard(tenantID, currentStart, currentEnd, "kas_masuk");
        Integer prevTotalKasMasuk = orderRepo.sumDashboard(tenantID, prevStart, currentStart, "kas_masuk");
        Integer totalKasMasukIncreasePercent = calculatePercent(prevTotalKasMasuk, totalKasMasuk);

        Integer totalKasKeluar = orderRepo.sumDashboard(tenantID, currentStart, currentEnd, "kas_keluar");
        Integer prevTotalKasKeluar = orderRepo.sumDashboard(tenantID, prevStart, currentStart, "kas_keluar");
        Integer totalKasKeluarIncreasePercent = calculatePercent(prevTotalKasKeluar, totalKasKeluar);

        return new SummarizeResponse(
                totalKasMasuk,
                totalKasMasukIncreasePercent,
                totalKasKeluar,
                totalKasKeluarIncreasePercent,
                totalKasMasuk - totalKasKeluar);
    }

    public List<LatestWeekGraphResponse> getLastestWeekGraph() {
        // get sum kas_masuk and kas_keluar last 7 days
        // output: {total, date}
        UUID tenantID = TenantContext.getTenantId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last7week = now.minusWeeks(1);

        return orderRepo.getLastestWeekGraph(tenantID, last7week, now);
    }

    public List<CurrentActivity> currentActivity() {
        UUID tenantID = TenantContext.getTenantId();
        return orderRepo.findLatest(tenantID, PageRequest.of(0, 10));
    }
}
