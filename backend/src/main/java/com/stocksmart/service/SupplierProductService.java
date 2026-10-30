package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.AddSupplierProductRequest;
import com.stocksmart.dto.SupplierProductResponse;
import com.stocksmart.entity.Product;
import com.stocksmart.entity.Supplier;
import com.stocksmart.entity.SupplierProduct;
import com.stocksmart.exception.ConflictException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.ProductRepository;
import com.stocksmart.repository.SupplierProductRepository;
import com.stocksmart.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class SupplierProductService {

    private final SupplierProductRepository supplierProductRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public SupplierProductService(
            SupplierProductRepository supplierProductRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository) {
        this.supplierProductRepository = supplierProductRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    public List<SupplierProductResponse> listBySupplier(Long supplierId) {
        ensureSupplierExists(supplierId);
        return supplierProductRepository.findBySupplierIdOrderByIdAsc(supplierId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SupplierProductResponse> listByProduct(Long productId) {
        ensureProductExists(productId);
        return supplierProductRepository.findByProductIdOrderByIdAsc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SupplierProductResponse linkProductToSupplier(Long supplierId, AddSupplierProductRequest request) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (supplierProductRepository.existsBySupplierIdAndProductId(supplierId, request.productId())) {
            throw new ConflictException("Product is already linked to this supplier");
        }

        SupplierProduct link = new SupplierProduct();
        link.setSupplier(supplier);
        link.setProduct(product);
        link.setUnitCost(request.unitCost());

        supplierProductRepository.save(link);
        return toResponse(link);
    }

    @Transactional
    public void unlinkProductFromSupplier(Long supplierId, Long productId) {
        SupplierProduct link = supplierProductRepository.findBySupplierIdAndProductId(supplierId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product link not found for supplier"));
        supplierProductRepository.delete(link);
    }

    private void ensureSupplierExists(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier not found");
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
    }

    private SupplierProductResponse toResponse(SupplierProduct link) {
        return new SupplierProductResponse(
                link.getId(),
                link.getSupplier().getId(),
                link.getSupplier().getName(),
                link.getProduct().getId(),
                link.getProduct().getName(),
                link.getProduct().getSku(),
                link.getUnitCost());
    }
}
