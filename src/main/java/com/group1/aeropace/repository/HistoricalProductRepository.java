package com.group1.aeropace.repository;

import com.group1.aeropace.entity.HistoricalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface HistoricalProductRepository extends JpaRepository<HistoricalProduct, Long> {

    // Đóng record đang hiệu lực (valid_to = NULL) của một product
    @Modifying
    @Query("UPDATE HistoricalProduct hp SET hp.validTo = :now WHERE hp.productId = :productId AND hp.validTo IS NULL")
    void closeCurrentSnapshot(@Param("productId") Long productId, @Param("now") LocalDateTime now);

    // Temporal lookup: tìm snapshot hiệu lực tại thời điểm orderDate
    @Query("""
        SELECT hp FROM HistoricalProduct hp
        WHERE hp.productId = :productId
          AND hp.validFrom <= :orderDate
          AND (hp.validTo IS NULL OR hp.validTo > :orderDate)
        """)
    Optional<HistoricalProduct> findByProductIdAndDate(
            @Param("productId") Long productId,
            @Param("orderDate") LocalDateTime orderDate
    );

    // Fallback: snapshot sớm nhất — dùng khi order được đặt trước khi snapshot đầu tiên tồn tại
    @Query("SELECT hp FROM HistoricalProduct hp WHERE hp.productId = :productId ORDER BY hp.validFrom ASC LIMIT 1")
    Optional<HistoricalProduct> findEarliestByProductId(@Param("productId") Long productId);

    // Kiểm tra đã có snapshot nào chưa (dùng khi quyết định tạo initial snapshot)
    boolean existsByProductId(Long productId);
}
