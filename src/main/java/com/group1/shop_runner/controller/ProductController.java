package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.product.response.ProductDetailResponse;
import com.group1.shop_runner.dto.product.response.ProductResponse;
import com.group1.shop_runner.dto.product.response.ProductVariantResponse;
import com.group1.shop_runner.service.ProductService;
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

    // =========================================================
    // PUBLIC API 1: GET /api/v1/products/{id}
    // Mục đích:
    // - Lấy chi tiết 1 sản phẩm theo id
    // - Chỉ trả về nếu status = ACTIVE (ném 404 nếu DELETED/ARCHIVED/DRAFT)
    // =========================================================
    @GetMapping("/{id}")
    public ProductDetailResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // =========================================================
    // PUBLIC API 2: GET /api/v1/products/{id}/variants
    // Mục đích:
    // - Lấy danh sách variant của 1 sản phẩm (chỉ variant chưa bị xóa)
    // =========================================================
    @GetMapping("/{id}/variants")
    public List<ProductVariantResponse> getVariantsByProduct(@PathVariable Long id) {
        return productService.getVariantsByProduct(id);
    }

    // =========================================================
    // PUBLIC API 3: GET /api/v1/products/detail/{id}
    // Mục đích:
    // - Lấy 1 sản phẩm detail (kèm images, variants, categories)
    // - Chỉ trả về nếu status = ACTIVE
    // =========================================================
    @GetMapping("/detail/{id}")
    public ProductResponse getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    // =========================================================
    // PUBLIC API 4: GET /api/v1/products/by-ids
    // Mục đích:
    // - Lấy nhiều sản phẩm detail theo list id
    // - Chỉ trả về sản phẩm có status = ACTIVE
    // =========================================================
    @GetMapping("/by-ids")
    public List<ProductResponse> getProductsByIds(@RequestParam List<Long> ids) {
        return productService.getProductsByIds(ids);
    }

    // =========================================================
    // PUBLIC API 5: GET /api/v1/products/detail
    // Mục đích:
    // - Lấy tất cả sản phẩm ACTIVE (có phân trang)
    // =========================================================
    @GetMapping("/detail")
    public Map<String, Object> getAllProductDetail(
            @RequestParam(defaultValue = "0") int page
    ) {
        return productService.getAllProductDetail(page);
    }

    // =========================================================
    // PUBLIC API 6: GET /api/v1/products/filter
    // Mục đích:
    // - Filter sản phẩm ACTIVE theo tên, brand, category, giá
    // =========================================================
    @GetMapping("/filter")
    public Map<String, Object> filterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page
    ) {
        return productService.filterProducts(
                name, brands, categories, minPrice, maxPrice, page
        );
    }
}