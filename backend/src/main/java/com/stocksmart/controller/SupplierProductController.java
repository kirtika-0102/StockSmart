package com.stocksmart.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stocksmart.dto.AddSupplierProductRequest;
import com.stocksmart.dto.SupplierProductResponse;
import com.stocksmart.service.SupplierProductService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class SupplierProductController {

    private final SupplierProductService supplierProductService;

    public SupplierProductController(SupplierProductService supplierProductService) {
        this.supplierProductService = supplierProductService;
    }

    @GetMapping("/suppliers/{supplierId}/products")
    public List<SupplierProductResponse> listBySupplier(@PathVariable Long supplierId) {
        return supplierProductService.listBySupplier(supplierId);
    }

    @PostMapping("/suppliers/{supplierId}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<SupplierProductResponse> linkProduct(
            @PathVariable Long supplierId,
            @Valid @RequestBody AddSupplierProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierProductService.linkProductToSupplier(supplierId, request));
    }

    @DeleteMapping("/suppliers/{supplierId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public void unlinkProduct(
            @PathVariable Long supplierId,
            @PathVariable Long productId) {
        supplierProductService.unlinkProductFromSupplier(supplierId, productId);
    }

    @GetMapping("/products/{productId}/suppliers")
    public List<SupplierProductResponse> listByProduct(@PathVariable Long productId) {
        return supplierProductService.listByProduct(productId);
    }
}
