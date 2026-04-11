package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.product.request.ProductImageRequest;
import com.group1.shop_runner.dto.product.response.ProductImageResponse;
import com.group1.shop_runner.entity.Product;
import com.group1.shop_runner.entity.ProductImage;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.ProductImageRepository;
import com.group1.shop_runner.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    // =========================================================
    // API 1: GET /api/v1/products/{productId}/images
    // Mục đích:
    // - Lấy toàn bộ ảnh của 1 sản phẩm theo đúng thứ tự position
    // =========================================================
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getImagesByProductId(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByPositionAsc(productId);

        return images.stream()
                .map(this::mapToProductImageResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // API 2: GET /api/v1/products/images/{id}
    // Mục đích:
    // - Lấy chi tiết 1 ảnh theo id
    // =========================================================
    @Transactional(readOnly = true)
    public ProductImageResponse getImageById(Long id) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        return mapToProductImageResponse(image);
    }

    // =========================================================
    // API 3: POST /api/v1/products/images
    // Mục đích:
    // - Thêm mới 1 ảnh cho product
    // =========================================================
    @Transactional
    public ProductImageResponse createImage(ProductImageRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(request.getImageUrl());
        image.setPosition(request.getPosition());
        image.setCreatedAt(LocalDateTime.now());
        image.setUpdatedAt(LocalDateTime.now());

        ProductImage savedImage = productImageRepository.save(image);

        return mapToProductImageResponse(savedImage);
    }

    // =========================================================
    // API 4: PUT /api/v1/products/images/{id}
    // Mục đích:
    // - Cập nhật ảnh theo id
    // =========================================================
    @Transactional
    public ProductImageResponse updateImage(Long id, ProductImageRequest request) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        image.setProduct(product);
        image.setImageUrl(request.getImageUrl());
        image.setPosition(request.getPosition());
        image.setUpdatedAt(LocalDateTime.now());

        ProductImage updatedImage = productImageRepository.save(image);

        return mapToProductImageResponse(updatedImage);
    }

    // =========================================================
    // API 5: DELETE /api/v1/products/images/{id}
    // Mục đích:
    // - Xóa ảnh theo id
    // =========================================================
    @Transactional
    public void deleteImage(Long id) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        productImageRepository.delete(image);
    }

    // =========================================================
    // MAPPER:
    // Chuyển ProductImage entity -> ProductImageResponse
    // =========================================================
    private ProductImageResponse mapToProductImageResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getImageUrl(),
                image.getPosition()
        );
    }
}