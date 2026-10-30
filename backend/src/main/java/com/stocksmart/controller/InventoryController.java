package com.stocksmart.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocksmart.dto.BarcodeLookupResponse;
import com.stocksmart.dto.InventoryResponse;
import com.stocksmart.dto.InventoryTransactionResponse;
import com.stocksmart.dto.StockAdjustmentRequest;
import com.stocksmart.dto.StockMovementRequest;
import com.stocksmart.dto.UpdateReorderLevelRequest;
import com.stocksmart.service.InventoryService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("isAuthenticated()")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> listAll() {
        return inventoryService.listAll();
    }

    @GetMapping("/locations/{locationId}")
    public List<InventoryResponse> listByLocation(@PathVariable Long locationId) {
        return inventoryService.listByLocation(locationId);
    }

    @GetMapping("/locations/{locationId}/products/{productId}")
    public InventoryResponse getByLocationAndProduct(
            @PathVariable Long locationId,
            @PathVariable Long productId) {
        return inventoryService.getByLocationAndProduct(locationId, productId);
    }

    @GetMapping("/barcode/{barcode}")
    public BarcodeLookupResponse lookupByBarcode(@PathVariable String barcode) {
        return inventoryService.lookupByBarcode(barcode);
    }

    @GetMapping("/transactions")
    public List<InventoryTransactionResponse> listTransactions() {
        return inventoryService.listTransactions();
    }

    @GetMapping("/locations/{locationId}/transactions")
    public List<InventoryTransactionResponse> listTransactionsByLocation(@PathVariable Long locationId) {
        return inventoryService.listTransactionsByLocation(locationId);
    }

    @PostMapping("/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<InventoryResponse> stockIn(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.stockIn(request));
    }

    @PostMapping("/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'STAFF')")
    public ResponseEntity<InventoryResponse> stockOut(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(inventoryService.stockOut(request));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<InventoryResponse> adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjust(request));
    }

    @PutMapping("/reorder-level")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<InventoryResponse> updateReorderLevel(@Valid @RequestBody UpdateReorderLevelRequest request) {
        return ResponseEntity.ok(inventoryService.updateReorderLevel(request));
    }
}
