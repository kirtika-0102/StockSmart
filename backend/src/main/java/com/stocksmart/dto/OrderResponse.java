package com.stocksmart.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.stocksmart.entity.OrderStatus;

public record OrderResponse(
        Long id,
        Long locationId,
        String locationName,
        OrderStatus status,
        Instant orderDate,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt
) {
}
