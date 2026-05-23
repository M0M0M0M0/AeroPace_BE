package com.group1.shop_runner.repository;

import com.group1.shop_runner.entity.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndProductVariantId(Long userId, Long productVariantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(Long userId);
}