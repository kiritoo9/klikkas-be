package com.klikkas.dto.order_categories;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCategoryResponse(
        UUID id,
        String name,
        String description,
        String category_type,
        Boolean is_active,
        LocalDateTime created_at) {
}
