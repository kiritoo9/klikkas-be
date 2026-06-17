package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.klikkas.entity.ProductCategory;

public interface ProductCategoryRepository extends
        JpaRepository<ProductCategory, UUID>,
        JpaSpecificationExecutor<ProductCategory> {

    Optional<ProductCategory> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Optional<ProductCategory> findByIdAndDeletedAtIsNullAndTenantId(UUID id, UUID tenantID);
}