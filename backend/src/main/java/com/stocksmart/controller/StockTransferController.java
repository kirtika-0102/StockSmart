package com.stocksmart.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocksmart.dto.CreateTransferRequest;
import com.stocksmart.dto.StockTransferResponse;
import com.stocksmart.service.StockTransferService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/transfers")
@PreAuthorize("isAuthenticated()")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    public StockTransferController(StockTransferService stockTransferService) {
        this.stockTransferService = stockTransferService;
    }

    @GetMapping
    public List<StockTransferResponse> listAll() {
        return stockTransferService.listAll();
    }

    @GetMapping("/{id}")
    public StockTransferResponse get(@PathVariable Long id) {
        return stockTransferService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<StockTransferResponse> create(@Valid @RequestBody CreateTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockTransferService.create(request));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<StockTransferResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransferService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<StockTransferResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransferService.cancel(id));
    }
}
