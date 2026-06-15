package com.group1.aeropace.repository;

import com.group1.aeropace.dto.product.ProductVariantDto;
import com.group1.aeropace.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct_IdAndIsDeletedFalse(Long productId);

    Optional<ProductVariant> findFirstByProductIdAndIsDeletedFalseOrderByIdAsc(Long productId);

    @Query("""
        SELECT v FROM ProductVariant v
        JOIN FETCH v.product
        JOIN InventoryItem ii ON ii.productVariant = v
        WHERE ii.cachedStock <= :threshold
        AND v.isDeleted = false
        ORDER BY ii.cachedStock ASC
    """)
    List<ProductVariant> findLowStockVariants(@Param("threshold") int threshold);

    @Query("""
        SELECT new com.group1.aeropace.dto.product.ProductVariantDto(
            v.product.id,
            v.id,
            v.option1Value,
            v.option2Value,
            v.option3Value,
            v.price,
            v.comparePrice,
            (SELECT ii.cachedStock FROM InventoryItem ii WHERE ii.productVariant.id = v.id),
            v.sku,
            v.isDeleted
        )
        FROM ProductVariant v
        WHERE v.product.id IN :ids
        AND v.isDeleted = false
    """)
    List<ProductVariantDto> getVariantsByProductIds(List<Long> ids);
}
