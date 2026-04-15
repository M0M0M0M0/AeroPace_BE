package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.product.request.ProductRequest;
import com.group1.shop_runner.dto.product.request.ProductVariantRequest;
import com.group1.shop_runner.dto.product.response.ProductDetailResponse;
import com.group1.shop_runner.dto.product.response.ProductResponse;
import com.group1.shop_runner.dto.product.response.ProductVariantResponse;
import com.group1.shop_runner.entity.Product;
import com.group1.shop_runner.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    // =========================================================
    // ADMIN API 1: GET /api/v1/admin/products/detail
    // Mục đích:
    // - Lấy tất cả sản phẩm kể cả DELETED (dành cho admin quản lý)
    // =========================================================
    @GetMapping("/detail")
    public Map<String, Object> getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page
    ) {
        return productService.getAllProductDetailForAdmin(page);
    }

    // =========================================================
    // ADMIN API 2: GET /api/v1/admin/products/detail/{id}
    // Mục đích:
    // - Lấy chi tiết 1 sản phẩm theo id kể cả DELETED
    // =========================================================
    @GetMapping("/detail/{id}")
    public ProductResponse getProductDetailForAdmin(@PathVariable Long id) {
        return productService.getProductDetailForAdmin(id);
    }

    // =========================================================
    // ADMIN API 3: GET /api/v1/admin/products/by-ids
    // Mục đích:
    // - Lấy nhiều sản phẩm theo list id kể cả DELETED
    // =========================================================
    @GetMapping("/by-ids")
    public List<ProductResponse> getProductsByIdsForAdmin(@RequestParam List<Long> ids) {
        return productService.getProductsByIds(ids);
    }

    // =========================================================
    // ADMIN API 4: GET /api/v1/admin/products/filter
    // Mục đích:
    // - Filter sản phẩm theo các điều kiện (admin thấy tất cả status)
    // =========================================================
    @GetMapping("/filter")
    public Map<String, Object> filterProductsForAdmin(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<Product.Status> statuses,
            @RequestParam(defaultValue = "0") int page
    ) {
        return productService.filterProductsForAdmin(
                name, brands, categories, minPrice, maxPrice, statuses, page
        );
    }

    // =========================================================
    // ADMIN API 5: GET /api/v1/admin/products/{id}
    // Mục đích:
    // - Lấy chi tiết entity product (dùng cho form chỉnh sửa)
    // =========================================================
    @GetMapping("/{id}")
    public ProductDetailResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // =========================================================
    // ADMIN API 6: GET /api/v1/admin/products/{id}/variants
    // Mục đích:
    // - Lấy danh sách variant của 1 sản phẩm
    // =========================================================
    @GetMapping("/{id}/variants")
    public List<ProductVariantResponse> getVariantsByProduct(@PathVariable Long id) {
        return productService.getVariantsByProduct(id);
    }

    // =========================================================
    // ADMIN API 7: POST /api/v1/admin/products
    // Mục đích:
    // - Tạo mới 1 sản phẩm
    // =========================================================
    @PostMapping
    public ProductDetailResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    // =========================================================
    // ADMIN API 8: POST /api/v1/admin/products/variants
    // Mục đích:
    // - Tạo mới 1 variant cho product
    // =========================================================
    @PostMapping("/variants")
    public ProductVariantResponse createVariant(@Valid @RequestBody ProductVariantRequest request) {
        return productService.createVariant(request);
    }

    // =========================================================
    // ADMIN API 9: PUT /api/v1/admin/products/{id}
    // Mục đích:
    // - Cập nhật thông tin product
    // =========================================================
    @PutMapping("/{id}")
    public ProductDetailResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    // =========================================================
    // ADMIN API 10: PUT /api/v1/admin/products/variants/{id}
    // Mục đích:
    // - Cập nhật thông tin variant
    // =========================================================
    @PutMapping("/variants/{id}")
    public ProductVariantResponse updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        return productService.updateVariant(id, request);
    }

    // =========================================================
    // ADMIN API 11: DELETE /api/v1/admin/products/{id}
    // Mục đích:
    // - Soft delete product (chuyển status -> DELETED)
    // =========================================================
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "Delete product successfully";
    }

    // =========================================================
    // ADMIN API 12: DELETE /api/v1/admin/products/variants/{id}
    // Mục đích:
    // - Soft/hard delete variant tùy theo có order hay không
    // =========================================================
    @DeleteMapping("/variants/{id}")
    public String deleteVariant(@PathVariable Long id) {
        productService.deleteVariant(id);
        return "Delete variant successfully";
    }

    // =========================================================
    // ADMIN API 13: PATCH /api/v1/admin/products/{id}/status
    // Mục đích:
    // - Cập nhật status của product (ACTIVE / DRAFT / ARCHIVED / DELETED)
    // =========================================================
    @PatchMapping("/{id}/status")
    public String updateProductStatus(
            @PathVariable Long id,
            @RequestParam Product.Status status
    ) {
        productService.updateProductStatus(id, status);
        return "Update product status successfully";
    }
}