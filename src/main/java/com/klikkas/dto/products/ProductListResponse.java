package com.klikkas.dto.products;

import java.util.List;

public record ProductListResponse(
    List<ProductResponse> data,
    Integer page,
    Integer totalPage
) {}
