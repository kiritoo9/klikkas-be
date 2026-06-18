package com.klikkas.dto.orders;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderItemResponse(
                UUID id,
                UUID product_id,
                String item_name,
                String item_desc,
                Integer price,
                Integer qty,
                String remark,

                LocalDateTime createdAt) {
}
