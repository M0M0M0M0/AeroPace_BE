package com.group1.aeropace.repository;

import com.group1.aeropace.entity.Review;
import com.group1.aeropace.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    Optional<Review> findByOrderIdAndProductIdAndStatus(Long orderId, Long productId, ReviewStatus status);

    @Query("""
            SELECT r FROM Review r
            WHERE r.product.id = :productId
              AND r.status = 'ACTIVE'
              AND (:rating IS NULL OR r.rating = :rating)
            """)
    Page<Review> findActiveByProduct(
            @Param("productId") Long productId,
            @Param("rating") BigDecimal rating,
            Pageable pageable
    );

    @Query("""
            SELECT r.rating, COUNT(r)
            FROM Review r
            WHERE r.product.id = :productId
              AND r.status = 'ACTIVE'
            GROUP BY r.rating
            """)
    List<Object[]> countByRatingForProduct(@Param("productId") Long productId);

    @Query("""
            SELECT COUNT(r), AVG(r.rating)
            FROM Review r
            WHERE r.product.id = :productId
              AND r.status = 'ACTIVE'
            """)
    List<Object[]> getCountAndAverage(@Param("productId") Long productId);

    @Modifying
    @Query("""
            UPDATE Review r SET r.status = 'EXPIRED'
            WHERE r.status = 'PENDING'
              AND r.createdAt < :cutoff
            """)
    int expireOldPendingReviews(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
            SELECT r FROM Review r
            WHERE (:productId IS NULL OR r.product.id = :productId)
              AND (:status IS NULL OR r.status = :status)
              AND (:rating IS NULL OR r.rating = :rating)
            """)
    Page<Review> findAllForAdmin(
            @Param("productId") Long productId,
            @Param("status") ReviewStatus status,
            @Param("rating") BigDecimal rating,
            Pageable pageable
    );

    @Query("SELECT r FROM Review r WHERE r.order.id = :orderId AND r.status = 'ACTIVE'")
    List<Review> findActiveByOrderId(@Param("orderId") Long orderId);
}