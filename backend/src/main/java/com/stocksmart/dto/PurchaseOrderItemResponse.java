package com.stocksmart.dto;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantityOrdered,
        Integer quantityReceived,
        BigDecimal unitCost
) {
}
