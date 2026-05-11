package com.group1.shop_runner.dto.cart.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long cartItemId;
    private Long productVariantId;
    private Long productId;
    private String productName;
    private String option1Value;
    private String option2Value;
    private String option3Value;
    private BigDecimal price;
    private Integer quantity;
    private Integer stockAvailable;
    private BigDecimal lineTotal;
    private String image;
    private Integer stock;
}