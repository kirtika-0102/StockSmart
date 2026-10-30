package com.stocksmart.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 200)
        String name,

        @NotBlank(message = "SKU is required")
        @Size(max = 100)
        String sku,

        @Size(max = 100)
        String barcode,

        @Size(max = 1000)
        String description,

        @PositiveOrZero(message = "Unit price must be zero or positive")
        BigDecimal unitPrice,

        Long categoryId
) {
}
