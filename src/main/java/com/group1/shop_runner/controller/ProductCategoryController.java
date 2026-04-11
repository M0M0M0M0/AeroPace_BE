package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.product.response.ProductCategoryResponse;
import com.group1.shop_runner.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
//@CrossOrigin(origins = "*")

public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    // =========================================================
    // API 1: GET /api/v1/products/{productId}/categories
    // =========================================================
    @GetMapping("/{productId}/categories")
    public List<ProductCategoryResponse> getCategoriesByProductId(@PathVariable Long productId) {
        return productCategoryService.getCategoriesByProductId(productId);
    }

    // =========================================================
    // API 2: POST /api/v1/products/{productId}/categories/{categoryId}
    // =========================================================
    @PostMapping("/{productId}/categories/{categoryId}")
    public ProductCategoryResponse assignCategoryToProduct(@PathVariable Long productId,
                                                           @PathVariable Long categoryId) {
        return productCategoryService.assignCategoryToProduct(productId, categoryId);
    }

    // =========================================================
    // API 3: DELETE /api/v1/products/{productId}/categories/{categoryId}
    // =========================================================
    @DeleteMapping("/{productId}/categories/{categoryId}")
    public String removeCategoryFromProduct(@PathVariable Long productId,
                                            @PathVariable Long categoryId) {
        productCategoryService.removeCategoryFromProduct(productId, categoryId);
        return "Remove category from product successfully";
    }
}