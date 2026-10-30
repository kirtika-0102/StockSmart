package com.stocksmart.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreatePurchaseOrderRequest(
        @NotNull(message = "Supplier ID is required")
        Long supplierId,

        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotEmpty(message = "At least one item is required")
        List<@Valid PurchaseOrderItemRequest> items
) {
}
