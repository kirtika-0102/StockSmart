package com.stocksmart.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.InventoryValuationReportItem;
import com.stocksmart.entity.Inventory;
import com.stocksmart.entity.Product;
import com.stocksmart.repository.InventoryRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final InventoryRepository inventoryRepository;

    public ReportService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<InventoryValuationReportItem> getInventoryValuationReport() {
        List<Inventory> inventories = inventoryRepository.findAll();
        return inventories.stream()
                .map(inv -> {
                    Product p = inv.getProduct();
                    BigDecimal price = p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal total = price.multiply(BigDecimal.valueOf(inv.getQuantity()));
                    String category = p.getCategory() != null ? p.getCategory().getName() : "Uncategorized";

                    return new InventoryValuationReportItem(
                            p.getId(),
                            p.getName(),
                            p.getSku(),
                            category,
                            inv.getLocation().getName(),
                            inv.getQuantity(),
                            price,
                            total);
                })
                .toList();
    }
}
