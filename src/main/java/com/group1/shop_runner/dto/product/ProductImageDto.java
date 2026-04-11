package com.group1.shop_runner.dto.product;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductImageDto {
    private Long id;
    private Long productId;
    private String imageUrl;
    private int position;

    public ProductImageDto(Long id, Long productId, String imageUrl, Integer position) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.position = position;
    }
}

