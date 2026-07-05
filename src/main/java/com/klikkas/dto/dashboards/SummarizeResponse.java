package com.klikkas.dto.dashboards;

public record SummarizeResponse(
    Integer total_kas_masuk,
    Integer total_kas_masuk_increase_percent,
    Integer total_kas_keluar,
    Integer total_kas_keluar_increase_percent,
    Integer saldo_akhir
) {}
