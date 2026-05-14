package com.group1.shop_runner.repository;

import com.group1.shop_runner.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);
    boolean existsByProductVariantId(Long productVariantId);
    boolean existsByProductVariant_Product_Id(Long productId);
    @Query("""
        SELECT pv.product.id, SUM(oi.quantity)
        FROM OrderItem oi
        JOIN oi.productVariant pv
        JOIN oi.order o
        WHERE o.createdAt >= :from
          AND o.createdAt <= :to
          AND o.status <> 'CANCELLED'
        GROUP BY pv.product.id
        ORDER BY SUM(oi.quantity) DESC
        LIMIT :limit
    """)
    List<Object[]> findBestSellerProductIds(
            @Param("from")  LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("limit") int limit
    );
}
