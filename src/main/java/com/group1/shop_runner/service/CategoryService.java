package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.category.request.CategoryRequest;
import com.group1.shop_runner.dto.category.reponse.CategoryResponse;
import com.group1.shop_runner.entity.Category;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // =========================================================
    // API 1: GET /api/v1/categories
    // Lấy toàn bộ category
    // =========================================================
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // API 2: GET /api/v1/categories/{id}
    // Lấy category theo id
    // =========================================================
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return mapToResponse(category);
    }

    // =========================================================
    // API 3: POST /api/v1/categories
    // Tạo mới category
    // =========================================================
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        categoryRepository.findByName(request.getName())
                .ifPresent(c -> {
                    throw new AppException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
                });

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    // =========================================================
    // API 4: PUT /api/v1/categories/{id}
    // Cập nhật category
    // =========================================================
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new AppException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
                    }
                });

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);

        return mapToResponse(updated);
    }

    // =========================================================
    // API 5: DELETE /api/v1/categories/{id}
    // Xóa category
    // =========================================================
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }

    // =========================================================
    // MAPPER
    // =========================================================
    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}