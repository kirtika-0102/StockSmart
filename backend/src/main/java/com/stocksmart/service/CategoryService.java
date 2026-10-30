package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CategoryResponse;
import com.stocksmart.dto.CreateCategoryRequest;
import com.stocksmart.dto.UpdateCategoryRequest;
import com.stocksmart.entity.Category;
import com.stocksmart.exception.BusinessException;
import com.stocksmart.exception.ConflictException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.CategoryRepository;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> list() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse get(Long id) {
        return toResponse(findCategory(id));
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category name is already used");
        }
        Category category = new Category();
        category.setName(name);
        category.setParent(resolveParent(request.parentId()));
        categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = findCategory(id);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Category name is already used");
        }
        Category parent = resolveParent(request.parentId());
        assertNoCycle(category, parent);
        category.setName(name);
        category.setParent(parent);
        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findCategory(id);
        if (categoryRepository.countByParent_Id(id) > 0) {
            throw new ConflictException("Cannot delete a category that has subcategories");
        }
        categoryRepository.delete(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return findCategory(parentId);
    }

    private void assertNoCycle(Category category, Category parent) {
        Category cursor = parent;
        while (cursor != null) {
            if (cursor.getId().equals(category.getId())) {
                throw new BusinessException("A category cannot be nested under itself");
            }
            cursor = cursor.getParent();
        }
    }

    private CategoryResponse toResponse(Category category) {
        Category parent = category.getParent();
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                parent == null ? null : parent.getId(),
                parent == null ? null : parent.getName());
    }
}
