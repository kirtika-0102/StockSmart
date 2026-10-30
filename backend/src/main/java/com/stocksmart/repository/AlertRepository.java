package com.stocksmart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.Alert;
import com.stocksmart.entity.AlertStatus;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();

    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    Optional<Alert> findByProductIdAndLocationIdAndStatus(Long productId, Long locationId, AlertStatus status);

    List<Alert> findByProductIdAndLocationId(Long productId, Long locationId);

    long countByStatus(AlertStatus status);
}
