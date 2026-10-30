package com.stocksmart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateReorderLevelRequest(
        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Reorder level is required")
        @PositiveOrZero(message = "Reorder level cannot be negative")
        Integer reorderLevel
) {
}
