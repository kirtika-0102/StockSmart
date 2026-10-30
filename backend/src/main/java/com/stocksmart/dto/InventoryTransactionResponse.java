package com.stocksmart.dto;

import java.time.Instant;

import com.stocksmart.entity.TransactionType;

public record InventoryTransactionResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Long locationId,
        String locationName,
        TransactionType transactionType,
        Integer quantityChange,
        String referenceType,
        String referenceId,
        String username,
        Instant createdAt
) {
}
