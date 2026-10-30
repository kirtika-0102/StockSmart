package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CreatePurchaseOrderRequest;
import com.stocksmart.dto.PurchaseOrderItemRequest;
import com.stocksmart.dto.PurchaseOrderItemResponse;
import com.stocksmart.dto.PurchaseOrderResponse;
import com.stocksmart.dto.StockMovementRequest;
import com.stocksmart.entity.Location;
import com.stocksmart.entity.Product;
import com.stocksmart.entity.PurchaseOrder;
import com.stocksmart.entity.PurchaseOrderItem;
import com.stocksmart.entity.PurchaseOrderStatus;
import com.stocksmart.entity.Supplier;
import com.stocksmart.exception.BusinessException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.LocationRepository;
import com.stocksmart.repository.ProductRepository;
import com.stocksmart.repository.PurchaseOrderRepository;
import com.stocksmart.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            LocationRepository locationRepository,
            ProductRepository productRepository,
            InventoryService inventoryService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    public List<PurchaseOrderResponse> listAll() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public PurchaseOrderResponse get(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional
    public PurchaseOrderResponse create(CreatePurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setLocation(location);
        po.setStatus(PurchaseOrderStatus.SUBMITTED);

        for (PurchaseOrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantityOrdered(itemReq.quantity());
            item.setQuantityReceived(0);
            item.setUnitCost(itemReq.unitCost());
            po.addItem(item);
        }

        purchaseOrderRepository.save(po);
        return toResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse receive(Long id) {
        PurchaseOrder po = findOrder(id);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessException("Purchase order is already received");
        }
        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException("Cannot receive a cancelled purchase order");
        }

        for (PurchaseOrderItem item : po.getItems()) {
            inventoryService.stockIn(new StockMovementRequest(
                    po.getLocation().getId(),
                    item.getProduct().getId(),
                    item.getQuantityOrdered(),
                    "PURCHASE_ORDER",
                    "PO-" + po.getId(),
                    "PO receipt from " + po.getSupplier().getName()));
            item.setQuantityReceived(item.getQuantityOrdered());
        }

        po.setStatus(PurchaseOrderStatus.RECEIVED);
        return toResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder po = findOrder(id);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessException("Cannot cancel an already received purchase order");
        }
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return toResponse(po);
    }

    private PurchaseOrder findOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> itemResponses = po.getItems().stream()
                .map(i -> new PurchaseOrderItemResponse(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getProduct().getSku(),
                        i.getQuantityOrdered(),
                        i.getQuantityReceived(),
                        i.getUnitCost()))
                .toList();

        return new PurchaseOrderResponse(
                po.getId(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getLocation().getId(),
                po.getLocation().getName(),
                po.getStatus(),
                po.getOrderDate(),
                itemResponses,
                po.getCreatedAt());
    }
}
