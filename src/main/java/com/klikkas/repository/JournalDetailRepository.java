package com.klikkas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.klikkas.dto.cashflows.CashflowAccountDetail;
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
}
