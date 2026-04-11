package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.product.response.ProductCategoryResponse;
import com.group1.shop_runner.entity.Category;
import com.group1.shop_runner.entity.Product;
import com.group1.shop_runner.entity.ProductCategory;
import com.group1.shop_runner.entity.ProductCategoryId;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.CategoryRepository;
import com.group1.shop_runner.repository.ProductCategoryRepository;
import com.group1.shop_runner.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // =========================================================
    // API 1: GET /api/v1/products/{productId}/categories
    // Lấy tất cả category của 1 product
    // =========================================================
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getCategoriesByProductId(Long productId) {
        List<ProductCategory> productCategories =
                productCategoryRepository.findByProduct_Id(productId);

        return productCategories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // API 2: POST /api/v1/products/{productId}/categories/{categoryId}
    // Gán 1 category vào 1 product
    // =========================================================
    @Transactional
    public ProductCategoryResponse assignCategoryToProduct(Long productId, Long categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        productCategoryRepository.findByProduct_IdAndCategory_Id(productId, categoryId)
                .ifPresent(pc -> {
                    throw new AppException(ErrorCode.PRODUCT_CATEGORY_ALREADY_EXISTS);
                });

        ProductCategory productCategory = new ProductCategory();
        productCategory.setId(new ProductCategoryId(productId, categoryId));
        productCategory.setProduct(product);
        productCategory.setCategory(category);

        ProductCategory saved = productCategoryRepository.save(productCategory);

        return mapToResponse(saved);
    }

    // =========================================================
    // API 3: DELETE /api/v1/products/{productId}/categories/{categoryId}
    // Bỏ 1 category khỏi 1 product
    // =========================================================
    @Transactional
    public void removeCategoryFromProduct(Long productId, Long categoryId) {
        ProductCategory productCategory = productCategoryRepository
                .findByProduct_IdAndCategory_Id(productId, categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        productCategoryRepository.delete(productCategory);
    }

    private ProductCategoryResponse mapToResponse(ProductCategory productCategory) {
        return new ProductCategoryResponse(
                productCategory.getProduct().getId(),
                productCategory.getCategory().getId(),
                productCategory.getCategory().getName(),
                productCategory.getCategory().getDescription()
        );
    }
}