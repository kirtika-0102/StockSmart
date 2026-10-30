package com.stocksmart.dto;

import java.math.BigDecimal;

public record InventoryValuationReportItem(
        Long productId,
        String productName,
        String productSku,
        String categoryName,
        String locationName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalValue
) {
}
