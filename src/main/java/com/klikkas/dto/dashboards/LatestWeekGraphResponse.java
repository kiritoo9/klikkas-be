package com.klikkas.dto.dashboards;

import java.time.LocalDateTime;

public record LatestWeekGraphResponse(
        Long total,
        LocalDateTime order_date,
        String order_type) {
}
