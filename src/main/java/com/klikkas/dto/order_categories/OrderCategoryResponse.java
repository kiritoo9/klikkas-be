package com.klikkas.dto.order_categories;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCategoryResponse(
                UUID id,

                UUID debit_account_id,
                String debit_account_name,

                UUID credit_account_id,
                String credit_account_name,

                String name,
                String description,
                String category_type,
                Boolean is_active,
                LocalDateTime created_at) {
}
