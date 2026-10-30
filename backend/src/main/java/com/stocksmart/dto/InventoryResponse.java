package com.stocksmart.dto;

import com.stocksmart.entity.LocationType;

public record InventoryResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        String barcode,
        Long locationId,
        String locationName,
        LocationType locationType,
        Integer quantity,
        Integer reorderLevel,
        boolean lowStock
) {
}
