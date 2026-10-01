package com.stocksmart.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String message,
        List<FieldErrorDetail> errors,
        Instant timestamp
) {
    public record FieldErrorDetail(String field, String message) {
    }
}
