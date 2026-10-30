package com.stocksmart.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.BarcodeLookupResponse;
import com.stocksmart.dto.InventoryResponse;
import com.stocksmart.dto.InventoryTransactionResponse;
import com.stocksmart.dto.ProductResponse;
import com.stocksmart.dto.StockAdjustmentRequest;
import com.stocksmart.dto.StockMovementRequest;
import com.stocksmart.dto.UpdateReorderLevelRequest;
import com.stocksmart.entity.Inventory;
import com.stocksmart.entity.InventoryTransaction;
import com.stocksmart.entity.Location;
import com.stocksmart.entity.Product;
import com.stocksmart.entity.TransactionType;
import com.stocksmart.entity.User;
import com.stocksmart.exception.BusinessException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.InventoryRepository;
import com.stocksmart.repository.InventoryTransactionRepository;
import com.stocksmart.repository.LocationRepository;
import com.stocksmart.repository.ProductRepository;
import com.stocksmart.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryTransactionRepository transactionRepository,
            LocationRepository locationRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<InventoryResponse> listAll() {
        return inventoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InventoryResponse> listByLocation(Long locationId) {
        ensureLocationExists(locationId);
        return inventoryRepository.findByLocationId(locationId).stream()
                .map(this::toResponse)
                .toList();
    }

    public InventoryResponse getByLocationAndProduct(Long locationId, Long productId) {
        ensureLocationExists(locationId);
        ensureProductExists(productId);
        return inventoryRepository.findByProductIdAndLocationId(productId, locationId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No inventory record for product at this location"));
    }

    @Transactional
    public InventoryResponse stockIn(StockMovementRequest request) {
        Location location = findLocation(request.locationId());
        Product product = findProduct(request.productId());

        Inventory inventory = inventoryRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> createEmptyInventory(product, location));

        int newQuantity = inventory.getQuantity() + request.quantity();
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);

        TransactionType type = "PURCHASE_ORDER".equalsIgnoreCase(request.referenceType())
                ? TransactionType.PO_RECEIVE
                : TransactionType.STOCK_IN;

        recordTransaction(product, location, type, request.quantity(), request.referenceType(), request.referenceId());

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse stockOut(StockMovementRequest request) {
        Location location = findLocation(request.locationId());
        Product product = findProduct(request.productId());

        Inventory inventory = inventoryRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseThrow(() -> new BusinessException(
                        "Insufficient stock: product is not stocked at location " + location.getName()));

        int currentQuantity = inventory.getQuantity();
        if (currentQuantity < request.quantity()) {
            throw new BusinessException(
                    "Insufficient stock: available quantity is " + currentQuantity + ", requested " + request.quantity());
        }

        inventory.setQuantity(currentQuantity - request.quantity());
        inventoryRepository.save(inventory);

        TransactionType type = "ORDER".equalsIgnoreCase(request.referenceType())
                ? TransactionType.ORDER_FULFILL
                : TransactionType.STOCK_OUT;

        recordTransaction(product, location, type, -request.quantity(), request.referenceType(), request.referenceId());

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse adjust(StockAdjustmentRequest request) {
        Location location = findLocation(request.locationId());
        Product product = findProduct(request.productId());

        Inventory inventory = inventoryRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> createEmptyInventory(product, location));

        int oldQuantity = inventory.getQuantity();
        int delta = request.newQuantity() - oldQuantity;

        inventory.setQuantity(request.newQuantity());
        inventoryRepository.save(inventory);

        recordTransaction(product, location, TransactionType.ADJUSTMENT, delta, "ADJUSTMENT", request.reason());

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse updateReorderLevel(UpdateReorderLevelRequest request) {
        Location location = findLocation(request.locationId());
        Product product = findProduct(request.productId());

        Inventory inventory = inventoryRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> createEmptyInventory(product, location));

        inventory.setReorderLevel(request.reorderLevel());
        inventoryRepository.save(inventory);

        return toResponse(inventory);
    }

    public BarcodeLookupResponse lookupByBarcode(String barcode) {
        String trimmed = barcode.trim();
        Product product = productRepository.findByBarcodeIgnoreCase(trimmed)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with barcode: " + trimmed));

        List<Inventory> balances = inventoryRepository.findByProductId(product.getId());
        int totalQty = balances.stream().mapToInt(Inventory::getQuantity).sum();

        ProductResponse prodResponse = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getBarcode(),
                product.getDescription(),
                product.getUnitPrice(),
                product.getCategory() == null ? null : product.getCategory().getId(),
                product.getCategory() == null ? null : product.getCategory().getName());

        List<InventoryResponse> balanceResponses = balances.stream().map(this::toResponse).toList();

        return new BarcodeLookupResponse(prodResponse, totalQty, balanceResponses);
    }

    public List<InventoryTransactionResponse> listTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    public List<InventoryTransactionResponse> listTransactionsByLocation(Long locationId) {
        ensureLocationExists(locationId);
        return transactionRepository.findByLocationIdOrderByCreatedAtDesc(locationId).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private Location findLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void ensureLocationExists(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location not found");
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
    }

    private Inventory createEmptyInventory(Product product, Location location) {
        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setLocation(location);
        inv.setQuantity(0);
        inv.setReorderLevel(0);
        return inv;
    }

    private void recordTransaction(
            Product product,
            Location location,
            TransactionType type,
            int quantityChange,
            String referenceType,
            String referenceId) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setLocation(location);
        tx.setTransactionType(type);
        tx.setQuantityChange(quantityChange);
        tx.setReferenceType(referenceType);
        tx.setReferenceId(referenceId);
        tx.setUser(resolveCurrentUser());
        transactionRepository.save(tx);
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    private InventoryResponse toResponse(Inventory inventory) {
        Product p = inventory.getProduct();
        Location l = inventory.getLocation();
        boolean isLow = inventory.getQuantity() <= inventory.getReorderLevel();
        return new InventoryResponse(
                inventory.getId(),
                p.getId(),
                p.getName(),
                p.getSku(),
                p.getBarcode(),
                l.getId(),
                l.getName(),
                l.getType(),
                inventory.getQuantity(),
                inventory.getReorderLevel(),
                isLow);
    }

    private InventoryTransactionResponse toTransactionResponse(InventoryTransaction tx) {
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
