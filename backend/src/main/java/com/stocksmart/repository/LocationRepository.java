package com.stocksmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.Location;
import com.stocksmart.entity.LocationType;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Location> findByTypeOrderByNameAsc(LocationType type);
}
