package com.klikkas.dto.orders;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OrderRequest(
        @NotNull(message = "Category ID is required")
        UUID category_id,

        @NotBlank(message = "Order date is required")
        String order_date,

        @NotBlank(message = "Order type is required")
        @Pattern(regexp = "kas_masuk|kas_keluar")
        String order_type,

        Integer tax_amount,
        Integer discount_amount,

        @Pattern(regexp = "pending|paid|canceled")
        String status,

        String remark,

        List<OrderItemRequest> items
) {
}
