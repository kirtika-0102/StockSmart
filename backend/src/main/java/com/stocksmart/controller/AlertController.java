package com.stocksmart.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stocksmart.dto.AlertResponse;
import com.stocksmart.entity.AlertStatus;
import com.stocksmart.service.AlertService;

@Validated
@RestController
@RequestMapping("/api/alerts")
@PreAuthorize("isAuthenticated()")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> list(@RequestParam(required = false) AlertStatus status) {
        return alertService.list(status);
    }

    @GetMapping("/{id}")
    public AlertResponse get(@PathVariable Long id) {
        return alertService.get(id);
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<AlertResponse> acknowledge(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.acknowledge(id));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<AlertResponse> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolve(id));
    }

    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'STAFF')")
    public ResponseEntity<List<AlertResponse>> checkAlerts() {
        return ResponseEntity.ok(alertService.checkAndGenerateAlerts());
    }
}
