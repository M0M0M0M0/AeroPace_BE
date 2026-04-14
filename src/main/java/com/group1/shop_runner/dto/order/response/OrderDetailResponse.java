package com.group1.shop_runner.dto.order.response;

import com.group1.shop_runner.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDetailResponse {
    private Integer id;
    private Long userId;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String shippingAddress;
    private String phoneNumber;
    private String receiverName;
    private String note;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}