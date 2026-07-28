package com.klikkas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.klikkas.dto.cashflows.CashflowAccountDetail;
import com.klikkas.dto.cashflows.CashflowDetailResponse;
import com.klikkas.dto.cashflows.CashflowListResponse;
import com.klikkas.dto.cashflows.LabaRugiAccountItem;
import com.klikkas.dto.cashflows.LabaRugiResponse;
import com.klikkas.dto.cashflows.SumByCategory;
import com.klikkas.dto.cashflows.SummarizeResponse;
import com.klikkas.entity.Order;
import com.klikkas.repository.JournalDetailRepository;
import com.klikkas.repository.OrderRepository;
import com.klikkas.security.TenantContext;
import com.klikkas.specification.OrderSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashFlowService {

        private final OrderRepository orderRepo;
        private final JournalDetailRepository journalDetailRepo;

        public SummarizeResponse getSummarize(
                        LocalDate dateFrom,
                        LocalDate dateTo) {

                UUID tenantID = TenantContext.getTenantId();

                // get opening balance
                Specification<Order> spec = Specification
                                .where(OrderSpecification.notDeleted())
                                .and(OrderSpecification.byTenant(tenantID))
                                .and(OrderSpecification.byCategoryName("kas awal"));

                String order = "createdAt";
                Page<Order> page = orderRepo
                                .findAll(spec, PageRequest.of(
                                                0,
                                                1,
                                                Sort.by(order).descending()));

                Order latest = page.hasContent() ? page.getContent().get(0) : null;
                Integer openingBalance = latest != null ? latest.getGrandTotal() : 0;
                LocalDateTime openingPeriod = latest != null ? latest.getCreatedAt() : null;

                // get summarize cash by range date
                Integer grandTotalIN = orderRepo.sumCashFlow(
                                tenantID,
                                dateFrom.atStartOfDay(),
                                dateTo.atTime(LocalTime.MAX),
                                "kas_masuk");

                Integer grandTotalOUT = orderRepo.sumCashFlow(
                                tenantID,
                                dateFrom.atStartOfDay(),
                                dateTo.atTime(LocalTime.MAX),
                                "kas_keluar");

                Integer increasingAmount = grandTotalIN - grandTotalOUT;
                Integer finalCash = openingBalance + increasingAmount;

                return new SummarizeResponse(
                                openingBalance,
                                openingPeriod,
                                increasingAmount,
                                finalCash);
        }

        public List<CashflowListResponse> getCashflowList(
                        LocalDate dateFrom,
                        LocalDate dateTo) {

                UUID tenantID = TenantContext.getTenantId();

                List<CashflowAccountDetail> details = journalDetailRepo.getCashflowDetails(
                                tenantID,
                                dateFrom.atStartOfDay(),
                                dateTo.atTime(LocalTime.MAX));

                // Group by account_code
                Map<String, List<CashflowDetailResponse>> groupedDetails = new LinkedHashMap<>();
                Map<String, Integer> accountTotals = new LinkedHashMap<>();
                Map<String, String> accountNames = new LinkedHashMap<>();

                for (CashflowAccountDetail detail : details) {
                        String accountCode = detail.account_code();
                        String accountName = detail.account_name();
                        String category = detail.category();
                        String type = detail.type();
                        Integer amount = detail.amount();

                        // Track account name
                        accountNames.put(accountCode, accountName);

                        // Group details by account_code and category+type
                        String detailKey = accountCode + "|" + category + "|" + type;
                        groupedDetails.computeIfAbsent(detailKey, k -> new ArrayList<>())
                                        .add(new CashflowDetailResponse(category, amount, type));

                        // Calculate total: kas_masuk adds, kas_keluar subtracts
                        int contribution = "kas_masuk".equals(type) ? amount : -amount;
                        accountTotals.merge(accountCode, contribution, Integer::sum);
                }

                // Build response grouped by account_code
                Map<String, List<CashflowDetailResponse>> accountGrouped = new LinkedHashMap<>();
                for (CashflowAccountDetail detail : details) {
                        String accountCode = detail.account_code();
                        String category = detail.category();
                        String type = detail.type();
                        Integer amount = detail.amount();

                        CashflowDetailResponse detailResponse = new CashflowDetailResponse(category, amount, type);

                        if (!accountGrouped.containsKey(accountCode)) {
                                accountGrouped.put(accountCode, new ArrayList<>());
                        }
                        // Avoid duplicates
                        boolean exists = accountGrouped.get(accountCode).stream()
                                        .anyMatch(d -> d.category().equals(category) && d.type().equals(type));
                        if (!exists) {
                                accountGrouped.get(accountCode).add(detailResponse);
                        }
                }

                List<CashflowListResponse> result = new ArrayList<>();
                for (Map.Entry<String, List<CashflowDetailResponse>> entry : accountGrouped.entrySet()) {
                        result.add(new CashflowListResponse(
                                        entry.getKey(),
                                        accountNames.get(entry.getKey()),
                                        accountTotals.get(entry.getKey()),
                                        entry.getValue()));
                }

                return result;
        }

        public LabaRugiResponse getLabaRugi(
                        LocalDate dateFrom,
                        LocalDate dateTo) {

                UUID tenantID = TenantContext.getTenantId();

                LocalDateTime startDate = dateFrom.atStartOfDay();
                LocalDateTime endDate = dateTo.atTime(LocalTime.MAX);

                List<LabaRugiAccountItem> pendapatanList = journalDetailRepo.getRevenueAccounts(tenantID, startDate,
                                endDate);
                List<LabaRugiAccountItem> bebanList = journalDetailRepo.getExpenseAccounts(tenantID, startDate,
                                endDate);

                Long totalPendapatan = pendapatanList.stream()
                                .mapToLong(LabaRugiAccountItem::amount)
                                .sum();

                Long totalBeban = bebanList.stream()
                                .mapToLong(LabaRugiAccountItem::amount)
                                .sum();

                Long labaBersih = totalPendapatan - totalBeban;

                return new LabaRugiResponse(
                                pendapatanList,
                                totalPendapatan,
                                bebanList,
                                totalBeban,
                                labaBersih);
        }

        public List<SumByCategory> getSumOrderByCategory(
                        LocalDate dateFrom,
                        LocalDate dateTo) {
                UUID tenantID = TenantContext.getTenantId();
                LocalDateTime startDate = dateFrom.atStartOfDay();
                LocalDateTime endDate = dateTo.atTime(LocalTime.MAX);

                List<SumByCategory> data = orderRepo.getSumOrderByCategory(tenantID, startDate, endDate);
                return data;
        }

}
