package com.stocksmart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.SupplierProduct;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {

    List<SupplierProduct> findBySupplierIdOrderByIdAsc(Long supplierId);

    List<SupplierProduct> findByProductIdOrderByIdAsc(Long productId);

    Optional<SupplierProduct> findBySupplierIdAndProductId(Long supplierId, Long productId);

    boolean existsBySupplierIdAndProductId(Long supplierId, Long productId);

    long countByProductId(Long productId);

    long countBySupplierId(Long supplierId);
}
