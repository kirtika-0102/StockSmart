package com.stocksmart.dto;

public record CategoryResponse(
        Long id,
        String name,
        Long parentId,
        String parentName
) {
}
