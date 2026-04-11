package com.group1.shop_runner.dto.product.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductImageRequest {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "Position is required")
    @Min(value = 1, message = "Position must be greater than 0")
    private Integer position;
}