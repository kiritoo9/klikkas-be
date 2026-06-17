package com.klikkas.dto.products;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(

    @NotNull(message = "Category ID is required")
    UUID category_id,

    @NotBlank(message = "SKU is required")
    String sku,

    @NotBlank(message = "Name is required")
    String name,

    String description,
    Integer buy_price,
    Integer sell_price,
    Integer stock,

    Boolean is_active
) {}
