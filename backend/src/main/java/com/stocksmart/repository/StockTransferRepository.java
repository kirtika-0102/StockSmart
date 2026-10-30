package com.stocksmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.StockTransfer;
import com.stocksmart.entity.TransferStatus;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    List<StockTransfer> findAllByOrderByCreatedAtDesc();

    List<StockTransfer> findByStatusOrderByCreatedAtDesc(TransferStatus status);

    List<StockTransfer> findBySourceLocationIdOrDestLocationIdOrderByCreatedAtDesc(Long sourceId, Long destId);
}
