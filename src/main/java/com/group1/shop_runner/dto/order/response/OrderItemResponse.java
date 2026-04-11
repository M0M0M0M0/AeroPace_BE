package com.group1.shop_runner.dto.order.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponse {
    private Integer productVariantId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
}