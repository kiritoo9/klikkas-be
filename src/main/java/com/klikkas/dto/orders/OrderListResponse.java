package com.klikkas.dto.orders;

import java.util.List;

public record OrderListResponse(
        List<OrderResponse> data,
        Integer page,
        Integer totalPage) {
}
