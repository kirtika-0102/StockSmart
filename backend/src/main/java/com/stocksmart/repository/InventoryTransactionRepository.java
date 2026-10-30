package com.stocksmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.InventoryTransaction;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryTransaction> findByLocationIdOrderByCreatedAtDesc(Long locationId);

    List<InventoryTransaction> findByProductIdAndLocationIdOrderByCreatedAtDesc(Long productId, Long locationId);

    List<InventoryTransaction> findAllByOrderByCreatedAtDesc();
}
