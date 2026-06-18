package com.klikkas.dto.orders;

import java.util.List;

public record OrderDetailResponse(
        OrderResponse order,
        List<OrderItemResponse> order_items) {
}
