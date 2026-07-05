package com.klikkas.dto.dashboards;

import java.time.LocalDateTime;
import java.util.UUID;

public record CurrentActivity(
                UUID id,
                LocalDateTime order_date,
                String order_type,
                String category_name,
                Integer grand_total) {
}
