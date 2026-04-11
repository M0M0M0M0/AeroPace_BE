package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.product.request.ProductImageRequest;
import com.group1.shop_runner.dto.product.response.ProductImageResponse;
import com.group1.shop_runner.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
//@CrossOrigin(origins = "*")

public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    // =========================================================
    // API 1: GET /api/v1/products/{productId}/images
    // Mục đích:
    // - Lấy danh sách ảnh của 1 sản phẩm
    // =========================================================
    @GetMapping("/{productId}/images")
    public List<ProductImageResponse> getImagesByProductId(@PathVariable Long productId) {
        return productImageService.getImagesByProductId(productId);
    }

    // =========================================================
    // API 2: GET /api/v1/products/images/{id}
    // Mục đích:
    // - Lấy 1 ảnh theo id
    // =========================================================
    @GetMapping("/images/{id}")
    public ProductImageResponse getImageById(@PathVariable Long id) {
        return productImageService.getImageById(id);
    }

    // =========================================================
    // API 3: POST /api/v1/products/images
    // Mục đích:
    // - Tạo mới ảnh cho sản phẩm
    // =========================================================
    @PostMapping("/images")
    public ProductImageResponse createImage(@Valid @RequestBody ProductImageRequest request) {
        return productImageService.createImage(request);
    }

    // =========================================================
    // API 4: PUT /api/v1/products/images/{id}
    // Mục đích:
    // - Cập nhật ảnh
    // =========================================================
    @PutMapping("/images/{id}")
    public ProductImageResponse updateImage(@PathVariable Long id,
                                            @Valid @RequestBody ProductImageRequest request) {
        return productImageService.updateImage(id, request);
    }

    // =========================================================
    // API 5: DELETE /api/v1/products/images/{id}
    // Mục đích:
    // - Xóa ảnh
    // =========================================================
    @DeleteMapping("/images/{id}")
    public String deleteImage(@PathVariable Long id) {
        productImageService.deleteImage(id);
        return "Delete product image successfully";
    }
}