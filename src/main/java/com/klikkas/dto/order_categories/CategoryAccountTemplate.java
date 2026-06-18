package com.klikkas.dto.order_categories;

public record CategoryAccountTemplate(
        String name,
        String description,
        String categoryType,
        String debitAccountCode,
        String creditAccountCode) {
}