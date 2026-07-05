package com.klikkas.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.klikkas.dto.cashflows.SummarizeResponse;
import com.klikkas.entity.Order;
import com.klikkas.repository.OrderRepository;
import com.klikkas.specification.OrderSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashFlowService {

    private final OrderRepository orderRepo;

    public SummarizeResponse getSummarize(
            LocalDate dateFrom,
            LocalDate dateTo) {

        // get opening balance
        Specification<Order> spec = Specification
                .where(OrderSpecification.notDeleted())
                .and(OrderSpecification.byCategoryName("kas awal"));

        String order = "createdAt";
        Page<Order> page = orderRepo
                .findAll(spec, PageRequest.of(
                        0,
                        1,
                        Sort.by(order).descending()));

        Order latest = page.hasContent() ? page.getContent().get(0) : null;
        Integer openingBalance = latest != null ? latest.getGrandTotal() : 0;

        // get summarize cash by range date
        Integer grandTotalIN = orderRepo.sumCashFlow(
                dateFrom.atStartOfDay(),
                dateTo.atTime(LocalTime.MAX),
                "kas_masuk");

        Integer grandTotalOUT = orderRepo.sumCashFlow(
                dateFrom.atStartOfDay(),
                dateTo.atTime(LocalTime.MAX),
                "kas_keluar");

        Integer increasingAmount = grandTotalIN - grandTotalOUT;
        Integer finalCash = openingBalance + increasingAmount;

        return new SummarizeResponse(
                openingBalance,
                increasingAmount,
                finalCash);
    }

}
