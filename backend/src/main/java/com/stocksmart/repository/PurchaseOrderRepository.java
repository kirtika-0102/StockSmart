package com.stocksmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.PurchaseOrder;
import com.stocksmart.entity.PurchaseOrderStatus;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(PurchaseOrderStatus status);

    List<PurchaseOrder> findByLocationIdOrderByCreatedAtDesc(Long locationId);

    List<PurchaseOrder> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);
}
