package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.AlertResponse;
import com.stocksmart.entity.Alert;
import com.stocksmart.entity.AlertStatus;
import com.stocksmart.entity.AlertType;
import com.stocksmart.entity.Inventory;
import com.stocksmart.entity.Location;
import com.stocksmart.entity.Product;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.AlertRepository;
import com.stocksmart.repository.InventoryRepository;

@Service
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;
    private final InventoryRepository inventoryRepository;

    public AlertService(AlertRepository alertRepository, InventoryRepository inventoryRepository) {
        this.alertRepository = alertRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<AlertResponse> list(AlertStatus status) {
        if (status != null) {
            return alertRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return alertRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public AlertResponse get(Long id) {
        return toResponse(findAlert(id));
    }

    @Transactional
    public AlertResponse acknowledge(Long id) {
        Alert alert = findAlert(id);
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        return toResponse(alert);
    }

    @Transactional
    public AlertResponse resolve(Long id) {
        Alert alert = findAlert(id);
        alert.setStatus(AlertStatus.RESOLVED);
        return toResponse(alert);
    }

    @Transactional
    public List<AlertResponse> checkAndGenerateAlerts() {
        List<Inventory> inventories = inventoryRepository.findAll();

        for (Inventory inv : inventories) {
            Product p = inv.getProduct();
            Location l = inv.getLocation();
            int qty = inv.getQuantity();
            int reorder = inv.getReorderLevel();

            if (qty <= reorder) {
                AlertType alertType = qty == 0 ? AlertType.OUT_OF_STOCK : AlertType.LOW_STOCK;
                String msg = alertType == AlertType.OUT_OF_STOCK
                        ? "Product " + p.getName() + " (" + p.getSku() + ") at " + l.getName() + " is OUT OF STOCK (0 units available; reorder level is " + reorder + ")"
                        : "Product " + p.getName() + " (" + p.getSku() + ") at " + l.getName() + " is LOW IN STOCK (" + qty + " units remaining; reorder level is " + reorder + ")";

                // Check if an active or acknowledged alert already exists
                boolean exists = alertRepository.findByProductIdAndLocationId(p.getId(), l.getId()).stream()
                        .anyMatch(a -> a.getStatus() == AlertStatus.ACTIVE || a.getStatus() == AlertStatus.ACKNOWLEDGED);

                if (!exists) {
                    Alert alert = new Alert();
                    alert.setProduct(p);
                    alert.setLocation(l);
                    alert.setAlertType(alertType);
                    alert.setMessage(msg);
                    alert.setStatus(AlertStatus.ACTIVE);
                    alertRepository.save(alert);
                }
            } else {
                // If quantity is now above reorder level, resolve existing active alerts
                List<Alert> existing = alertRepository.findByProductIdAndLocationId(p.getId(), l.getId());
                for (Alert a : existing) {
                    if (a.getStatus() == AlertStatus.ACTIVE || a.getStatus() == AlertStatus.ACKNOWLEDGED) {
                        a.setStatus(AlertStatus.RESOLVED);
                    }
                }
            }
        }

        return list(AlertStatus.ACTIVE);
    }

    private Alert findAlert(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getProduct().getId(),
                alert.getProduct().getName(),
                alert.getProduct().getSku(),
                alert.getLocation().getId(),
                alert.getLocation().getName(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getStatus(),
                alert.getCreatedAt());
    }
}
