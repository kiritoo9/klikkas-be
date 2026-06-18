package com.klikkas.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.Account;

public class AccountSpecification {

    public static Specification<Account> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Account> byId(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Account> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), like),
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("type")), like));
        };
    }

}
