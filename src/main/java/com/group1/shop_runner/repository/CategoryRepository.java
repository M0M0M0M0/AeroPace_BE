package com.group1.shop_runner.repository;

import com.group1.shop_runner.dto.category.CategoryDto;
import com.group1.shop_runner.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByName(String name);
    @Query("""
    SELECT new com.group1.shop_runner.dto.category.CategoryDto(
        pc.product.id,
        c.id,
        c.name
    )
    FROM ProductCategory pc
    JOIN pc.category c
    WHERE pc.product.id IN :ids
""")
    List<CategoryDto> getByProductIds(List<Long> ids);
}
