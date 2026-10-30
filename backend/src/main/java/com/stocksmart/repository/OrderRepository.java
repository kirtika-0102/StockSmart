package com.stocksmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.Order;
import com.stocksmart.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByLocationIdOrderByCreatedAtDesc(Long locationId);
}
