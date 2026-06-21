package com.group1.aeropace.repository;

import com.group1.aeropace.entity.ProductEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {
    Optional<ProductEmbedding> findByProductId(Long productId);
}
