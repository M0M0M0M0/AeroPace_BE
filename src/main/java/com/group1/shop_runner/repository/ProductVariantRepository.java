package com.group1.shop_runner.repository;

import com.group1.shop_runner.dto.product.ProductVariantDto;
import com.group1.shop_runner.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct_IdAndIsDeletedFalse(Long productId);

    Optional<ProductVariant> findFirstByProductIdAndIsDeletedFalseOrderByIdAsc(Long productId);

    @Query("""
        SELECT new com.group1.shop_runner.dto.product.ProductVariantDto(
            v.product.id,
            v.id,
            v.option1Value,
            v.option2Value,
            v.option3Value,
            v.price,
            v.comparePrice,
            v.stock,
            v.sku,
            v.isDeleted
        )
        FROM ProductVariant v
        WHERE v.product.id IN :ids
        AND v.isDeleted = false
    """)
    List<ProductVariantDto> getVariantsByProductIds(List<Long> ids);
}
