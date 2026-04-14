package com.group1.shop_runner.repository;

import com.group1.shop_runner.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndProductVariantId(Long userId, Long productVariantId);

    void deleteByUserId(Long userId);
}