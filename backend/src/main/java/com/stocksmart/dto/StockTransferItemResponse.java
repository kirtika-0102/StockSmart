package com.stocksmart.dto;

public record StockTransferItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity
) {
}
