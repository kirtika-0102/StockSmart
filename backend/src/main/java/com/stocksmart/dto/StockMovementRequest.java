package com.stocksmart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity,

        @Size(max = 50)
        String referenceType,

        @Size(max = 100)
        String referenceId,

        @Size(max = 255)
        String reason
) {
}
