package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.product.request.CategoryRequest;
import com.group1.shop_runner.dto.product.response.CategoryResponse;
import com.group1.shop_runner.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
//@CrossOrigin(origins = "*")

public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // =========================================================
    // API 1: GET /api/v1/categories
    // =========================================================
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // =========================================================
    // API 2: GET /api/v1/categories/{id}
    // =========================================================
    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    // =========================================================
    // API 3: POST /api/v1/categories
    // =========================================================
    @PostMapping
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    // =========================================================
    // API 4: PUT /api/v1/categories/{id}
    // =========================================================
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id,
                                           @Valid @RequestBody CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    // =========================================================
    // API 5: DELETE /api/v1/categories/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "Delete category successfully";
    }
}