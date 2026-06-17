package com.klikkas.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.ProductCategory;
import com.klikkas.entity.Tenant;

import jakarta.persistence.criteria.Join;

public class ProductCategorySpecification {

    public static Specification<ProductCategory> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ProductCategory> byTenant(UUID tenantID) {
        return (root, query, cb) -> {
            Join<ProductCategory, Tenant> tenant = root.join("tenant");
            return cb.equal(tenant.get("id"), tenantID);
        };
    }

    public static Specification<ProductCategory> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("isActive"), active);
    }

    public static Specification<ProductCategory> keyword(String k) {
        return (root, query, cb) -> {
            if (k == "" || k.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + k.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like));
        };
    }

}
