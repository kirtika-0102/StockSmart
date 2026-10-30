package com.stocksmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
