package com.stocksmart.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardKpiResponse(
        long totalProducts,
        long totalInventoryQuantity,
        BigDecimal totalInventoryValue,
        long lowStockCount,
        long outOfStockCount,
        long activeAlertsCount,
        long pendingPurchaseOrders,
        long totalOrders,
        List<CategoryStockSummary> categoryDistribution,
        List<LocationStockSummary> locationDistribution,
        List<InventoryTransactionResponse> recentTransactions
) {
}
