package com.klikkas.dto.orders;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull(message = "Category ID is required")
        UUID category_id,

        @NotBlank(message = "Order date is required")
        String order_date,

        Integer tax_amount,
        Integer discount_amount,
        String status,
        String remark,

        List<OrderItemRequest> items
) {
}
