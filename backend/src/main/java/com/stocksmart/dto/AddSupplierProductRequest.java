package com.stocksmart.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AddSupplierProductRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @PositiveOrZero(message = "Unit cost must be zero or positive")
        BigDecimal unitCost
) {
}
