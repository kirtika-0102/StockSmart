package com.stocksmart.dto;

import java.math.BigDecimal;

import com.stocksmart.entity.LocationType;

public record LocationStockSummary(
        Long locationId,
        String locationName,
        LocationType locationType,
        long totalQuantity,
        BigDecimal totalValue
) {
}
