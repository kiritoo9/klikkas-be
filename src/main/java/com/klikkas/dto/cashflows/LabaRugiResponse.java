package com.klikkas.dto.cashflows;

import java.util.List;

public record LabaRugiResponse(
    List<LabaRugiAccountItem> pendapatan,
    Long total_pendapatan,
    List<LabaRugiAccountItem> beban,
    Long total_beban,
    Long laba_bersih
) {}
