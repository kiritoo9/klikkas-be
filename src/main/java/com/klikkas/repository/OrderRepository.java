package com.klikkas.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.klikkas.entity.Order;

public interface OrderRepository extends
                JpaRepository<Order, UUID>,
                JpaSpecificationExecutor<Order> {

        Optional<Order> findByIdAndDeletedAtIsNullAndTenantId(UUID id, UUID tenantID);

        @Query("""
                            SELECT COALESCE(SUM(o.grandTotal), 0)
                            FROM Order o
                            WHERE o.deletedAt IS NULL
                                AND o.orderDate BETWEEN :startDate AND :endDate
                                AND LOWER(o.orderType) = :orderType
                                AND LOWER(o.category.name) != 'kas awal'
                                AND LOWER(o.status) = 'paid'
                        """)
        Integer sumCashFlow(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        String orderType);

}