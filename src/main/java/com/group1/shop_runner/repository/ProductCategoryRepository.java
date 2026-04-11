package com.group1.shop_runner.repository;

import com.group1.shop_runner.entity.ProductCategory;
import com.group1.shop_runner.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    List<ProductCategory> findByProduct_Id(Long productId);
    List<ProductCategory> findByCategory_Id(Long categoryId);
    Optional<ProductCategory> findByProduct_IdAndCategory_Id(Long productId, Long categoryId);
}