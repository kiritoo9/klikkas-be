package com.klikkas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.klikkas.dto.cashflows.CashflowAccountDetail;
import com.klikkas.dto.cashflows.LabaRugiAccountItem;
import com.klikkas.entity.JournalDetail;

public interface JournalDetailRepository extends
        JpaRepository<JournalDetail, UUID> {

        @Query("""
                SELECT new com.klikkas.dto.cashflows.CashflowAccountDetail(
                        a.code,
                        a.name,
                        oc.name,
                        COALESCE(jd.credit - jd.debit, 0),
                        oc.categoryType
                )
                FROM JournalDetail jd
                JOIN jd.journal j
                JOIN jd.account a
                JOIN Order o ON o.id = j.referenceId AND (j.referenceType = 'kas_masuk' OR j.referenceType = 'kas_keluar')
                JOIN o.category oc
                WHERE j.deletedAt IS NULL
                        AND jd.deletedAt IS NULL
                        AND a.deletedAt IS NULL
                        AND o.deletedAt IS NULL
                        AND oc.deletedAt IS NULL
                        AND o.tenant.id = :tenantID
                        AND j.journalDate BETWEEN :startDate AND :endDate
                """)
        List<CashflowAccountDetail> getCashflowDetails(
                        UUID tenantID,
                        LocalDateTime startDate,
                        LocalDateTime endDate);

        @Query("""
                SELECT new com.klikkas.dto.cashflows.LabaRugiAccountItem(
                        a.code,
                        a.name,
                        CASE WHEN SUM(jd.credit - jd.debit) IS NULL THEN 0 ELSE SUM(jd.credit - jd.debit) END
                )
                FROM JournalDetail jd
                JOIN jd.journal jr
                JOIN jd.account a
                WHERE jr.deletedAt IS NULL
                        AND jd.deletedAt IS NULL
                        AND a.deletedAt IS NULL
                        AND a.code LIKE '4%'
                        AND jr.journalDate BETWEEN :startDate AND :endDate
                GROUP BY a.code, a.name
                """)
        List<LabaRugiAccountItem> getRevenueAccounts(
                        LocalDateTime startDate,
                        LocalDateTime endDate);

        @Query("""
                SELECT new com.klikkas.dto.cashflows.LabaRugiAccountItem(
                        a.code,
                        a.name,
                        CASE WHEN SUM(jd.debit - jd.credit) IS NULL THEN 0 ELSE SUM(jd.debit - jd.credit) END
                )
                FROM JournalDetail jd
                JOIN jd.journal jr
                JOIN jd.account a
                WHERE jr.deletedAt IS NULL
                        AND jd.deletedAt IS NULL
                        AND a.deletedAt IS NULL
                        AND a.code LIKE '5%'
                        AND jr.journalDate BETWEEN :startDate AND :endDate
                GROUP BY a.code, a.name
                """)
        List<LabaRugiAccountItem> getExpenseAccounts(
                        LocalDateTime startDate,
                        LocalDateTime endDate);
}
