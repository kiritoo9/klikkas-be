package com.klikkas.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.klikkas.entity.UserTokenUsages;

public class UserTokenUsageSpecification {

    public static Specification<UserTokenUsages> byUser(UUID userID) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userID);
    }

    public static Specification<UserTokenUsages> keywords(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("usage_title")), like),
                    cb.like(cb.lower(root.get("llm_model")), like));
            
        };
    }
    
}
