package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.klikkas.entity.OrderCategory;

public interface OrderCategoryRepository extends
        JpaRepository<OrderCategory, UUID>,
        JpaSpecificationExecutor<OrderCategory> {

    Optional<OrderCategory> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    boolean existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(String name,
            UUID id);

    Optional<OrderCategory> findByIdAndDeletedAtIsNullAndTenantId(UUID id, UUID tenantID);

}