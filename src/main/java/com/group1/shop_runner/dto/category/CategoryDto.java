package com.group1.shop_runner.dto.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {
    private Long productId;
    private Long id;
    private String name;

    public CategoryDto(Long productId, Long id, String name) {
        this.productId = productId;
        this.id = id;
        this.name = name;
    }
}
