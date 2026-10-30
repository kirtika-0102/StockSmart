package com.stocksmart.dto;

import java.math.BigDecimal;

public record SupplierProductResponse(
        Long id,
        Long supplierId,
        String supplierName,
        Long productId,
        String productName,
        String productSku,
        BigDecimal unitCost
) {
}
