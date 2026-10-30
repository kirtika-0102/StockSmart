package com.stocksmart.dto;

import java.util.List;

public record BarcodeLookupResponse(
        ProductResponse product,
        Integer totalQuantity,
        List<InventoryResponse> balances
) {
}
