package com.group1.aeropace.service;

import com.group1.aeropace.dto.category.request.CategoryRequest;
import com.group1.aeropace.dto.category.reponse.CategoryResponse;
import com.group1.aeropace.entity.Category;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy toàn bộ category
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Tạo mới category
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

    // Cập nhật category
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

    // Xóa category
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }

    // MAPPER
    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
