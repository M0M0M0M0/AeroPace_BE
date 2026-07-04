package com.group1.aeropace.repository;

import com.group1.aeropace.entity.HistoricalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface HistoricalProductRepository extends JpaRepository<HistoricalProduct, Long> {

    @Modifying
    @Query("UPDATE HistoricalProduct hp SET hp.validTo = :now WHERE hp.productId = :productId AND hp.validTo IS NULL")
    void closeCurrentSnapshot(@Param("productId") Long productId, @Param("now") LocalDateTime now);

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

    @Query("SELECT hp FROM HistoricalProduct hp WHERE hp.productId = :productId ORDER BY hp.validFrom ASC LIMIT 1")
    Optional<HistoricalProduct> findEarliestByProductId(@Param("productId") Long productId);

    boolean existsByProductId(Long productId);
}
