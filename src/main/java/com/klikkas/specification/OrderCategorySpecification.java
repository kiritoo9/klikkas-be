package com.klikkas.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.OrderCategory;
import com.klikkas.entity.Tenant;

import jakarta.persistence.criteria.Join;

public class OrderCategorySpecification {

    public static Specification<OrderCategory> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<OrderCategory> byTenant(UUID tenantID) {
        return (root, query, cb) -> {
            Join<OrderCategory, Tenant> tenant = root.join("tenant");
            return cb.equal(tenant.get("id"), tenantID);
        };
    }

    public static Specification<OrderCategory> categoryType(String t) {
        return (root, query, cb) -> {
            if (t == "" || t.isBlank()) {
                return cb.conjunction();
            }

            return cb.equal(cb.lower(root.get("categoryType")), t.toLowerCase());
        };
    }

    public static Specification<OrderCategory> keyword(String k) {
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
