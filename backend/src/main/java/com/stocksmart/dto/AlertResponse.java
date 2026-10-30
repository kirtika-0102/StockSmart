package com.stocksmart.dto;

import java.time.Instant;

import com.stocksmart.entity.AlertStatus;
import com.stocksmart.entity.AlertType;

public record AlertResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Long locationId,
        String locationName,
        AlertType alertType,
        String message,
        AlertStatus status,
        Instant createdAt
) {
}
