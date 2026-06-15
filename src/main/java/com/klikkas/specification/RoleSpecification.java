package com.klikkas.specification;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.Role;

public class RoleSpecification {

    public static Specification<Role> notdeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Role> notRoot() {
        return (root, query, cb) -> cb.notEqual(cb.lower(root.get("name")), "super_root");
    }

    public static Specification<Role> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like));
        };
    }

}
