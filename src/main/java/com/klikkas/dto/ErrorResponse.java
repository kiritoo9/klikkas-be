package com.klikkas.dto;

public record ErrorResponse (
    String message,
    String error
) {}
