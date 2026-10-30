package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CreateProductRequest;
import com.stocksmart.dto.ProductResponse;
import com.stocksmart.dto.UpdateProductRequest;
import com.stocksmart.entity.Category;
import com.stocksmart.entity.Product;
import com.stocksmart.exception.ConflictException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.CategoryRepository;
import com.stocksmart.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponse> list() {
        return productRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse get(Long id) {
        return toResponse(findProduct(id));
    }

    public ProductResponse findByBarcode(String barcode) {
        Product product = productRepository.findByBarcodeIgnoreCase(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for barcode: " + barcode));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String sku = request.sku().trim();
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new ConflictException("SKU is already in use");
        }
        Product product = new Product();
        product.setName(request.name().trim());
        product.setSku(sku);
        product.setBarcode(trimOrNull(request.barcode()));
        product.setDescription(trimOrNull(request.description()));
        product.setUnitPrice(request.unitPrice());
        product.setCategory(resolveCategory(request.categoryId()));
        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findProduct(id);
        String sku = request.sku().trim();
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new ConflictException("SKU is already in use");
        }
        product.setName(request.name().trim());
        product.setSku(sku);
        product.setBarcode(trimOrNull(request.barcode()));
        product.setDescription(trimOrNull(request.description()));
        product.setUnitPrice(request.unitPrice());
        product.setCategory(resolveCategory(request.categoryId()));
        return toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ProductResponse toResponse(Product product) {
        Category category = product.getCategory();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getBarcode(),
                product.getDescription(),
                product.getUnitPrice(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName());
    }
}
