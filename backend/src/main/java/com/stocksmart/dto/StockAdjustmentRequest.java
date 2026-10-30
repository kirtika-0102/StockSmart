package com.stocksmart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "New quantity is required")
        @PositiveOrZero(message = "New quantity cannot be negative")
        Integer newQuantity,

        @Size(max = 255)
        String reason
) {
}
