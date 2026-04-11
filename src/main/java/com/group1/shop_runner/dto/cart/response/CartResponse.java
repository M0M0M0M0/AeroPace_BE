package com.group1.shop_runner.dto.cart.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long cartId;
    private Long userId;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private List<CartItemResponse> items;
}