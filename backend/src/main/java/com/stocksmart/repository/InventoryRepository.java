package com.stocksmart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndLocationId(Long productId, Long locationId);

    List<Inventory> findByLocationId(Long locationId);

    List<Inventory> findByProductId(Long productId);

    boolean existsByProductIdAndLocationId(Long productId, Long locationId);

    long countByProductId(Long productId);

    long countByLocationId(Long locationId);

    List<Inventory> findByQuantityLessThanEqual(Integer threshold);
}
