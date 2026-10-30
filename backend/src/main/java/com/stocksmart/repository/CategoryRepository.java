package com.stocksmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    long countByParent_Id(Long parentId);
}
