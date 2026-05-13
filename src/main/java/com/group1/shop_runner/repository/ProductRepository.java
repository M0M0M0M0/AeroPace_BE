package com.group1.shop_runner.repository;

import com.group1.shop_runner.dto.product.response.ProductResponse;
import com.group1.shop_runner.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);

    @Query("""
    SELECT new com.group1.shop_runner.dto.product.response.ProductResponse(
        p.id,
        p.name,
        p.slug,
        p.description,
        b.name,
        p.option1Name,
        p.option2Name,
        p.option3Name,
        p.status
    )
    FROM Product p
    LEFT JOIN p.brand b
    WHERE p.id IN :ids
""")
    List<ProductResponse> getProductsByIds(@Param("ids") List<Long> ids);

    @Query("""
                SELECT new com.group1.shop_runner.dto.product.response.ProductResponse(
                    p.id,
                    p.name,
                    p.slug,
                    p.description,
                    b.name,
                    p.option1Name,
                    p.option2Name,
                    p.option3Name,
                    p.status
                )
                FROM Product p
                LEFT JOIN p.brand b
                WHERE p.status = com.group1.shop_runner.entity.Product.Status.ACTIVE
            """)
    Page<ProductResponse> getProducts(Pageable pageable);

    @Query(
            value = """
        SELECT DISTINCT new com.group1.shop_runner.dto.product.response.ProductResponse(
            p.id, p.name, p.slug, p.description, b.name,
            p.option1Name, p.option2Name, p.option3Name, p.status
        )
        FROM Product p
        LEFT JOIN p.brand b
        LEFT JOIN p.variants v
        LEFT JOIN p.productCategories pc
        WHERE p.status = com.group1.shop_runner.entity.Product.Status.ACTIVE
        AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))
               OR EXISTS (
                   SELECT 1 FROM ProductCategory pc2
                   JOIN pc2.category cat
                   WHERE pc2.product = p
                   AND LOWER(cat.name) LIKE LOWER(CONCAT('%', :name, '%'))
               ))
        AND (:brandIds IS NULL OR b.id IN :brandIds)
        AND (:categoryIds IS NULL OR pc.category.id IN :categoryIds)
        AND (:minPrice IS NULL OR v.price >= :minPrice)
        AND (:maxPrice IS NULL OR v.price <= :maxPrice)
    """,
            countQuery = """
        SELECT COUNT(DISTINCT p.id)
        FROM Product p
        LEFT JOIN p.brand b
        LEFT JOIN p.variants v
        LEFT JOIN p.productCategories pc
        WHERE p.status = com.group1.shop_runner.entity.Product.Status.ACTIVE
        AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))
               OR EXISTS (
                   SELECT 1 FROM ProductCategory pc2
                   JOIN pc2.category cat
                   WHERE pc2.product = p
                   AND LOWER(cat.name) LIKE LOWER(CONCAT('%', :name, '%'))
               ))
        AND (:brandIds IS NULL OR b.id IN :brandIds)
        AND (:categoryIds IS NULL OR pc.category.id IN :categoryIds)
        AND (:minPrice IS NULL OR v.price >= :minPrice)
        AND (:maxPrice IS NULL OR v.price <= :maxPrice)
    """
    )
    Page<ProductResponse> filterProducts(
            @Param("name") String name,
            @Param("brandIds") List<Long> brandIds,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    // Lấy tất cả sản phẩm không filter status (admin)
    @Query("""
    SELECT new com.group1.shop_runner.dto.product.response.ProductResponse(
        p.id, p.name, p.slug, p.description, b.name,
        p.option1Name, p.option2Name, p.option3Name, p.status
    )
    FROM Product p
    LEFT JOIN p.brand b
""")
    Page<ProductResponse> getProductsForAdmin(Pageable pageable);

    //Filter cho admin
    @Query(
            value = """
SELECT DISTINCT new com.group1.shop_runner.dto.product.response.ProductResponse(
    p.id, p.name, p.slug, p.description, b.name,
    p.option1Name, p.option2Name, p.option3Name, p.status
)
FROM Product p
LEFT JOIN p.brand b
LEFT JOIN p.variants v
LEFT JOIN p.productCategories pc
WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
       OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))
       OR EXISTS (
           SELECT 1 FROM ProductCategory pc2
           JOIN pc2.category cat
           WHERE pc2.product = p
           AND LOWER(cat.name) LIKE LOWER(CONCAT('%', :name, '%'))
       ))
AND (:brandIds IS NULL OR b.id IN :brandIds)
AND (:categoryIds IS NULL OR pc.category.id IN :categoryIds)
AND (:minPrice IS NULL OR v.price >= :minPrice)
AND (:maxPrice IS NULL OR v.price <= :maxPrice)
AND (:statuses IS NULL OR p.status IN :statuses)
AND (:productId IS NULL OR p.id = :productId)
AND (:variantId IS NULL OR EXISTS (
    SELECT 1 FROM ProductVariant pv
    WHERE pv.product = p AND pv.id = :variantId
))
AND (:sku IS NULL OR EXISTS (
    SELECT 1 FROM ProductVariant pv
    WHERE pv.product = p AND LOWER(pv.sku) LIKE LOWER(CONCAT('%', :sku, '%'))
))
AND (:stockMin IS NULL OR (
    SELECT COALESCE(SUM(pv2.stock), 0) FROM ProductVariant pv2
    WHERE pv2.product = p AND pv2.isDeleted = false
) >= :stockMin)
AND (:stockMax IS NULL OR (
    SELECT COALESCE(SUM(pv2.stock), 0) FROM ProductVariant pv2
    WHERE pv2.product = p AND pv2.isDeleted = false
) <= :stockMax)
""",
            countQuery = """
SELECT COUNT(DISTINCT p.id)
FROM Product p
LEFT JOIN p.brand b
LEFT JOIN p.variants v
LEFT JOIN p.productCategories pc
WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
       OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))
       OR EXISTS (
           SELECT 1 FROM ProductCategory pc2
           JOIN pc2.category cat
           WHERE pc2.product = p
           AND LOWER(cat.name) LIKE LOWER(CONCAT('%', :name, '%'))
       ))
AND (:brandIds IS NULL OR b.id IN :brandIds)
AND (:categoryIds IS NULL OR pc.category.id IN :categoryIds)
AND (:minPrice IS NULL OR v.price >= :minPrice)
AND (:maxPrice IS NULL OR v.price <= :maxPrice)
AND (:statuses IS NULL OR p.status IN :statuses)
AND (:productId IS NULL OR p.id = :productId)
AND (:variantId IS NULL OR EXISTS (
    SELECT 1 FROM ProductVariant pv
    WHERE pv.product = p AND pv.id = :variantId
))
AND (:sku IS NULL OR EXISTS (
    SELECT 1 FROM ProductVariant pv
    WHERE pv.product = p AND LOWER(pv.sku) LIKE LOWER(CONCAT('%', :sku, '%'))
))
AND (:stockMin IS NULL OR (
    SELECT COALESCE(SUM(pv2.stock), 0) FROM ProductVariant pv2
    WHERE pv2.product = p AND pv2.isDeleted = false
) >= :stockMin)
AND (:stockMax IS NULL OR (
    SELECT COALESCE(SUM(pv2.stock), 0) FROM ProductVariant pv2
    WHERE pv2.product = p AND pv2.isDeleted = false
) <= :stockMax)
"""
    )
    Page<ProductResponse> filterProductsForAdmin(
            @Param("name") String name,
            @Param("brandIds") List<Long> brandIds,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("statuses") List<Product.Status> statuses,
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("sku") String sku,
            @Param("stockMin") Integer stockMin,
            @Param("stockMax") Integer stockMax,
            Pageable pageable
    );
}