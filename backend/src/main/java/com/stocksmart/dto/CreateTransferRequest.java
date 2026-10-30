package com.stocksmart.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateTransferRequest(
        @NotNull(message = "Source location ID is required")
        Long sourceLocationId,

        @NotNull(message = "Destination location ID is required")
        Long destLocationId,

        @NotEmpty(message = "At least one item is required")
        List<@Valid TransferItemRequest> items
) {
}
