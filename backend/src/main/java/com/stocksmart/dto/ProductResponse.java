package com.stocksmart.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        String barcode,
        String description,
        BigDecimal unitPrice,
        Long categoryId,
        String categoryName
) {
}
