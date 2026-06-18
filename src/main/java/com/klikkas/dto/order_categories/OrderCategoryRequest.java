package com.klikkas.dto.order_categories;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OrderCategoryRequest(

        @NotBlank(message = "Name is required") 
        String name,

        @NotBlank(message = "Category type is required")
        @Pattern(regexp = "kas_masuk|kas_keluar")
        String category_type,

        UUID debit_account_id,
        UUID credit_account_id,

        String description,
        Boolean is_active
) {}
