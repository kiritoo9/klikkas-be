package com.klikkas.dto.products;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID category_id,
        String category_name,

        String sku,
        String name,
        String description,
        Integer buy_price,
        Integer sell_price,
        Integer stock,

        LocalDateTime created_at) {
}
