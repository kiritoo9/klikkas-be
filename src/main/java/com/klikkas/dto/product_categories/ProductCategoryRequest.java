package com.klikkas.dto.product_categories;

import jakarta.validation.constraints.NotBlank;

public record ProductCategoryRequest(

        @NotBlank(message = "Name is required") 
        String name,

        String description,
        boolean is_active
) {}
