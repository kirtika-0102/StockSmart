package com.stocksmart.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocksmart.dto.InventoryValuationReportItem;
import com.stocksmart.service.ReportService;

@Validated
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("isAuthenticated()")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/valuation")
    public List<InventoryValuationReportItem> getValuationReport() {
        return reportService.getInventoryValuationReport();
    }
}
