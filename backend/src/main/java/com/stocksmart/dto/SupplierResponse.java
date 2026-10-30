package com.stocksmart.dto;

public record SupplierResponse(
        Long id,
        String name,
        String contactName,
        String email,
        String phone,
        String address
) {
}
