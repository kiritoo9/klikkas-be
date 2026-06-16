package com.klikkas.dto.product_categories;

import java.util.List;

public record ProductCategoryListResponse(
    List<ProductCategoryResponse> data,
    Integer page,
    Integer totalPage
) {}
