package com.klikkas.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.Product;
import com.klikkas.entity.ProductCategory;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class ProductSpecification {

    public static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Product> category(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }

            Join<Product, ProductCategory> category = root.join("category", JoinType.INNER);
            return cb.equal(category.get("id"), categoryId);
        };
    }

    public static Specification<Product> keyword(String k) {
        return (root, query, cb) -> {
            if (k == "" || k.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + k.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("sku")), like),
                    cb.like(cb.lower(root.get("name")), like));
        };
    }

}
