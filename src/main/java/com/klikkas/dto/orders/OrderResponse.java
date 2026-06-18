package com.klikkas.dto.orders;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID category_id,

        String no_order,
        LocalDateTime order_date,

        Integer total_qty,
        Integer total_price,
        Integer tax_amount,
        Integer discount_amount,
        Integer grand_total,
        String status,
        String remark,

        LocalDateTime createdAt) {
}