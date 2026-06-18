package com.group1.aeropace.controller;

import com.group1.aeropace.dto.product.response.ProductResponse;
import com.group1.aeropace.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/detail/{id}")
    public ProductResponse getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @GetMapping("/by-ids")
    public List<ProductResponse> getProductsByIds(@RequestParam List<Long> ids) {
        return productService.getProductsByIds(ids);
    }

    @GetMapping("/detail")
    public Map<String, Object> getAllProductDetail(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String sort
    ) {
        return productService.getAllProductDetail(page, sort);
    }

    @GetMapping("/filter")
    public Map<String, Object> filterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxRating,
            @RequestParam(required = false) Integer minReviewCount,
            @RequestParam(required = false) Integer maxReviewCount,
            @RequestParam(required = false) String sort
    ) {
        return productService.filterProducts(
                name, brands, categories, minPrice, maxPrice, page, minRating, maxRating, minReviewCount, maxReviewCount, sort
        );
    }
}
