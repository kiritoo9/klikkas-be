package com.klikkas.dto.order_categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OrderCategoryRequest(

        @NotBlank(message = "Name is required") 
        String name,

        @NotBlank(message = "Category type is required")
        @Pattern(regexp = "kas_masuk|kas_keluar")
        String category_type,

        String description,
        Boolean is_active
) {}
