package com.klikkas.dto.order_categories;

import java.util.List;

public record OrderCategoryListResponse(
        List<OrderCategoryResponse> data,
        Integer page,
        Integer totalPage) {
}
