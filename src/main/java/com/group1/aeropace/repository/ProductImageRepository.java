package com.group1.aeropace.repository;

import com.group1.aeropace.dto.product.ProductImageDto;


import com.group1.aeropace.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    List<ProductImage> findByProductIdOrderByPositionAsc(Long productId);
    List<ProductImage> findByProduct_Id(Long productId);
    @Query("""
    SELECT new com.group1.shop_runner.dto.product.ProductImageDto(
        i.id,
        i.product.id,
        i.imageUrl,
        i.position
    )
    FROM ProductImage i
    WHERE i.product.id IN :ids
""")
    List<ProductImageDto> getImagesByProductIds(List<Long> ids);
}
