package com.klikkas.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.Order;
import com.klikkas.entity.OrderCategory;
import com.klikkas.entity.Tenant;

import jakarta.persistence.criteria.Join;

public class OrderSpecification {

    public static Specification<Order> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Order> byType(String t) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("orderType")), t.toLowerCase());
    }

    public static Specification<Order> byCategoryName(String categoryName) {
        return (root, query, cb) -> {
            Join<Order, OrderCategory> join = root.join("category");
            return cb.equal(cb.lower(join.get("name")), categoryName);
        };
    }

    public static Specification<Order> byCategory(UUID categoryID) {
        return (root, query, cb) -> {
            if (categoryID == null) {
                return cb.conjunction();
            }

            Join<Order, OrderCategory> join = root.join("category");
            return cb.equal(join.get("id"), categoryID);
        };
    }

    public static Specification<Order> byTenant(UUID tenantID) {
        return (root, query, cb) -> {
            Join<Order, Tenant> join = root.join("tenant");
            return cb.equal(join.get("id"), tenantID);
        };
    }

    public static Specification<Order> keyword(String k) {
        return (root, query, cb) -> {
            if (k == "" || k.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + k.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("noOrder")), like),
                    cb.like(cb.lower(root.get("remark")), like));
        };
    }

}
