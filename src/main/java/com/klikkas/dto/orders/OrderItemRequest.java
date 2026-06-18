package com.klikkas.dto.orders;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record OrderItemRequest(
                UUID product_id,

                @NotBlank(message = "Item name is required") String item_name,

                String item_desc,
                Integer price,
                Integer qty,
                String remark) {
}
