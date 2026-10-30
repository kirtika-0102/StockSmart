package com.stocksmart.dto;

public record CategoryStockSummary(
        Long categoryId,
        String categoryName,
        long productCount
) {
}
