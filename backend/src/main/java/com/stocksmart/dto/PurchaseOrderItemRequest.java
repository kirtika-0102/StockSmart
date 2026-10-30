package com.stocksmart.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PurchaseOrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity ordered is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity,

        @PositiveOrZero(message = "Unit cost must be zero or positive")
        BigDecimal unitCost
) {
}
