package com.stocksmart.dto;

import java.time.Instant;
import java.util.List;

import com.stocksmart.entity.TransferStatus;

public record StockTransferResponse(
        Long id,
        Long sourceLocationId,
        String sourceLocationName,
        Long destLocationId,
        String destLocationName,
        TransferStatus status,
        List<StockTransferItemResponse> items,
        Instant createdAt
) {
}
