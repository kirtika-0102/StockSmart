package com.stocksmart.dto;

import java.time.Instant;
import java.util.List;

import com.stocksmart.entity.PurchaseOrderStatus;

public record PurchaseOrderResponse(
        Long id,
        Long supplierId,
        String supplierName,
        Long locationId,
        String locationName,
        PurchaseOrderStatus status,
        Instant orderDate,
        List<PurchaseOrderItemResponse> items,
        Instant createdAt
) {
}
