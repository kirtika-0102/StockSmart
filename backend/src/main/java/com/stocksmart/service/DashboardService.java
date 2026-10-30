package com.stocksmart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CategoryStockSummary;
import com.stocksmart.dto.DashboardKpiResponse;
import com.stocksmart.dto.InventoryTransactionResponse;
import com.stocksmart.dto.LocationStockSummary;
import com.stocksmart.entity.AlertStatus;
import com.stocksmart.entity.Category;
import com.stocksmart.entity.Inventory;
import com.stocksmart.entity.InventoryTransaction;
import com.stocksmart.entity.Location;
import com.stocksmart.entity.Product;
import com.stocksmart.entity.PurchaseOrderStatus;
import com.stocksmart.repository.AlertRepository;
import com.stocksmart.repository.CategoryRepository;
import com.stocksmart.repository.InventoryRepository;
import com.stocksmart.repository.InventoryTransactionRepository;
import com.stocksmart.repository.LocationRepository;
import com.stocksmart.repository.OrderRepository;
import com.stocksmart.repository.ProductRepository;
import com.stocksmart.repository.PurchaseOrderRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderRepository orderRepository;
    private final AlertRepository alertRepository;
    private final InventoryTransactionRepository transactionRepository;

    public DashboardService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            LocationRepository locationRepository,
            InventoryRepository inventoryRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            OrderRepository orderRepository,
            AlertRepository alertRepository,
            InventoryTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.inventoryRepository = inventoryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderRepository = orderRepository;
        this.alertRepository = alertRepository;
        this.transactionRepository = transactionRepository;
    }

    public DashboardKpiResponse getSummary() {
        long totalProducts = productRepository.count();

        List<Inventory> inventories = inventoryRepository.findAll();
        long totalQty = 0;
        BigDecimal totalVal = BigDecimal.ZERO;
        long lowStockCount = 0;
        long outOfStockCount = 0;

        Map<Long, Long> locationQtyMap = new HashMap<>();
        Map<Long, BigDecimal> locationValMap = new HashMap<>();

        for (Inventory inv : inventories) {
            int q = inv.getQuantity();
            int reorder = inv.getReorderLevel();
            totalQty += q;

            BigDecimal unitPrice = inv.getProduct().getUnitPrice() != null
                    ? inv.getProduct().getUnitPrice()
                    : BigDecimal.ZERO;
            BigDecimal lineVal = unitPrice.multiply(BigDecimal.valueOf(q));
            totalVal = totalVal.add(lineVal);

            if (q == 0 && reorder > 0) {
                outOfStockCount++;
            } else if (q <= reorder) {
                lowStockCount++;
            }

            Long locId = inv.getLocation().getId();
            locationQtyMap.put(locId, locationQtyMap.getOrDefault(locId, 0L) + q);
            locationValMap.put(locId, locationValMap.getOrDefault(locId, BigDecimal.ZERO).add(lineVal));
        }

        long activeAlerts = alertRepository.countByStatus(AlertStatus.ACTIVE);
        long pendingPOs = purchaseOrderRepository.findByStatusOrderByCreatedAtDesc(PurchaseOrderStatus.SUBMITTED).size();
        long totalOrders = orderRepository.count();

        // Category distribution
        List<Product> allProducts = productRepository.findAll();
        Map<String, Long> categoryCountMap = new HashMap<>();
        Map<String, Long> categoryIdMap = new HashMap<>();

        for (Product p : allProducts) {
            Category c = p.getCategory();
            String catName = c != null ? c.getName() : "Uncategorized";
            Long catId = c != null ? c.getId() : null;
            categoryCountMap.put(catName, categoryCountMap.getOrDefault(catName, 0L) + 1);
            if (catId != null) {
                categoryIdMap.put(catName, catId);
            }
        }

        List<CategoryStockSummary> categorySummaries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryCountMap.entrySet()) {
            categorySummaries.add(new CategoryStockSummary(
                    categoryIdMap.get(entry.getKey()),
                    entry.getKey(),
                    entry.getValue()));
        }

        // Location distribution
        List<Location> allLocations = locationRepository.findAll();
        List<LocationStockSummary> locationSummaries = new ArrayList<>();
        for (Location loc : allLocations) {
            long q = locationQtyMap.getOrDefault(loc.getId(), 0L);
            BigDecimal v = locationValMap.getOrDefault(loc.getId(), BigDecimal.ZERO);
            locationSummaries.add(new LocationStockSummary(
                    loc.getId(),
                    loc.getName(),
                    loc.getType(),
                    q,
                    v));
        }

        // Recent 8 transactions
        List<InventoryTransactionResponse> recentTxs = transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(8)
                .map(this::toTxResponse)
                .toList();

        return new DashboardKpiResponse(
                totalProducts,
                totalQty,
                totalVal,
                lowStockCount,
                outOfStockCount,
                activeAlerts,
                pendingPOs,
                totalOrders,
                categorySummaries,
                locationSummaries,
                recentTxs);
    }

    private InventoryTransactionResponse toTxResponse(InventoryTransaction tx) {
        return new InventoryTransactionResponse(
                tx.getId(),
                tx.getProduct().getId(),
                tx.getProduct().getName(),
                tx.getProduct().getSku(),
                tx.getLocation().getId(),
                tx.getLocation().getName(),
                tx.getTransactionType(),
                tx.getQuantityChange(),
                tx.getReferenceType(),
                tx.getReferenceId(),
                tx.getUser() != null ? tx.getUser().getUsername() : null,
                tx.getCreatedAt());
    }
}
