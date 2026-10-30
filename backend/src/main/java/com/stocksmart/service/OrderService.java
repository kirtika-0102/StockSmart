package com.stocksmart.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CreateOrderRequest;
import com.stocksmart.dto.OrderItemRequest;
import com.stocksmart.dto.OrderItemResponse;
import com.stocksmart.dto.OrderResponse;
import com.stocksmart.dto.StockMovementRequest;
import com.stocksmart.entity.Location;
import com.stocksmart.entity.Order;
import com.stocksmart.entity.OrderItem;
import com.stocksmart.entity.OrderStatus;
import com.stocksmart.entity.Product;
import com.stocksmart.exception.BusinessException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.LocationRepository;
import com.stocksmart.repository.OrderRepository;
import com.stocksmart.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public OrderService(
            OrderRepository orderRepository,
            LocationRepository locationRepository,
            ProductRepository productRepository,
            InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    public List<OrderResponse> listAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse get(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        Order order = new Order();
        order.setLocation(location);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            BigDecimal price = itemReq.unitPrice() != null
                    ? itemReq.unitPrice()
                    : (product.getUnitPrice() != null ? product.getUnitPrice() : BigDecimal.ZERO);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(price);
            order.addItem(item);

            total = total.add(price.multiply(BigDecimal.valueOf(itemReq.quantity())));
        }

        order.setTotalAmount(total);
        orderRepository.save(order);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse confirm(Long id) {
        Order order = findOrder(id);
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Order is already completed");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot confirm a cancelled order");
        }

        // Deduct inventory for each item (validates sufficient stock)
        for (OrderItem item : order.getItems()) {
            inventoryService.stockOut(new StockMovementRequest(
                    order.getLocation().getId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    "ORDER",
                    "ORD-" + order.getId(),
                    "Customer order fulfillment"));
        }

        order.setStatus(OrderStatus.COMPLETED);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = findOrder(id);
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel an already completed order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(order);
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getProduct().getSku(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getLocation().getId(),
                order.getLocation().getName(),
                order.getStatus(),
                order.getOrderDate(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt());
    }
}
