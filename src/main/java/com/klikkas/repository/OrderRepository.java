package com.klikkas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.klikkas.dto.cashflows.SumByCategory;
import com.klikkas.dto.dashboards.CurrentActivity;
import com.klikkas.dto.dashboards.LatestWeekGraphResponse;
import com.klikkas.entity.Order;

public interface OrderRepository extends
                JpaRepository<Order, UUID>,
                JpaSpecificationExecutor<Order> {

        Optional<Order> findByIdAndDeletedAtIsNullAndTenantId(UUID id, UUID tenantID);

        @Query("""
                            SELECT COALESCE(SUM(o.grandTotal), 0)
                            FROM Order o
                            WHERE o.deletedAt IS NULL
                                AND o.tenant.id = :tenantID
                                AND o.orderDate BETWEEN :startDate AND :endDate
                                AND LOWER(o.orderType) = :orderType
                                AND LOWER(o.category.name) != 'kas awal'
                                AND LOWER(o.status) = 'paid'
                        """)
        Integer sumCashFlow(
                        UUID tenantID,
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        String orderType);

        @Query("""
                            SELECT COALESCE(SUM(o.grandTotal), 0)
                            FROM Order o
                            WHERE o.deletedAt IS NULL
                                AND o.tenant.id = :tenantID
                                AND o.orderDate >= :startDate
                                AND o.orderDate < :endDate
                                AND LOWER(o.orderType) = :orderType
                                AND LOWER(o.category.name) != 'kas awal'
                                AND LOWER(o.status) = 'paid'
                        """)
        Integer sumDashboard(
                        UUID tenantID,
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        String orderType);

        @Query("""
                        SELECT
                        COALESCE(SUM(o.grandTotal), 0) AS total,
                        o.orderDate AS order_date,
                        o.orderType AS order_type
                        FROM Order o
                        WHERE o.deletedAt IS NULL
                        AND o.tenant.id = :tenantID
                        AND o.orderDate >= :startDate
                        AND o.orderDate < :endDate
                        AND LOWER(o.category.name) != 'kas awal'
                        AND LOWER(o.status) = 'paid'
                        GROUP BY o.orderDate, o.orderType
                        """)
        List<LatestWeekGraphResponse> getLastestWeekGraph(
                        UUID tenantID,
                        LocalDateTime startDate,
                        LocalDateTime endDate);

        @Query("""
                        SELECT new com.klikkas.dto.dashboards.CurrentActivity(
                                o.id,
                                o.orderDate,
                                o.orderType,
                                o.category.name,
                                o.grandTotal
                        )
                        FROM Order o
                        WHERE o.deletedAt IS NULL
                        AND o.tenant.id = :tenantID
                        ORDER BY o.createdAt DESC
                        """)
        List<CurrentActivity> findLatest(UUID tenantID, Pageable pageable);

        @Query("""
                        SELECT new com.klikkas.dto.cashflows.SumByCategory(
                                sum(o.grandTotal),
                                o.orderType,
                                c.name
                        )
                        FROM Order o
                        JOIN o.category c
                        WHERE o.deletedAt IS NULL
                                AND o.tenant.id = :tenantID
                                AND LOWER(c.name) != 'kas awal'
                                AND LOWER(o.status) = 'paid'
                                AND o.orderDate BETWEEN :startDate AND :endDate
                        GROUP BY c.name, o.orderType
                        """)
        List<SumByCategory> getSumOrderByCategory(
                        UUID tenantID,
                        LocalDateTime startDate,
                        LocalDateTime endDate);

}