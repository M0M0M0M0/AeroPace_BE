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
        p.option3Name
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
                    p.option3Name
                )
                FROM Product p
                LEFT JOIN p.brand b
            """)
    Page<ProductResponse> getProducts(Pageable pageable);
    @Query(
            value = """
        SELECT DISTINCT new com.group1.shop_runner.dto.product.response.ProductResponse(
            p.id, p.name, p.slug, p.description, b.name,
            p.option1Name, p.option2Name, p.option3Name
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
    """
    )
    Page<ProductResponse> filterProducts(
            @Param("name") String name,               // thêm
            @Param("brandIds") List<Long> brandIds,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

}
