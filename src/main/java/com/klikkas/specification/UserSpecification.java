package com.klikkas.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.User;

import jakarta.persistence.criteria.Join;

import com.klikkas.entity.Role;

public class UserSpecification {

    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<User> isNotRoot() {
        return (root, query, cb) -> {
            Join<User, Role> role = root.join("role");
            return cb.notEqual(cb.lower(role.get("name")), "super_root");
        };
    }

    public static Specification<User> byId(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<User> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("fullname")), like));
        };
    }

}
