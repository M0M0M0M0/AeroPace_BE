package com.group1.aeropace.controller;

import com.group1.aeropace.dto.product.response.ProductCategoryResponse;
import com.group1.aeropace.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
//@CrossOrigin(origins = "*")

public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @GetMapping("/{productId}/categories")
    public List<ProductCategoryResponse> getCategoriesByProductId(@PathVariable Long productId) {
        return productCategoryService.getCategoriesByProductId(productId);
    }

    @PostMapping("/{productId}/categories/{categoryId}")
    public ProductCategoryResponse assignCategoryToProduct(@PathVariable Long productId,
                                                           @PathVariable Long categoryId) {
        return productCategoryService.assignCategoryToProduct(productId, categoryId);
    }

    @DeleteMapping("/{productId}/categories/{categoryId}")
    public String removeCategoryFromProduct(@PathVariable Long productId,
                                            @PathVariable Long categoryId) {
        productCategoryService.removeCategoryFromProduct(productId, categoryId);
        return "Remove category from product successfully";
    }
}
