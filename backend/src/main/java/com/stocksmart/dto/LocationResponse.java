package com.stocksmart.dto;

import com.stocksmart.entity.LocationType;

public record LocationResponse(
        Long id,
        String name,
        LocationType type,
        String address
) {
}
