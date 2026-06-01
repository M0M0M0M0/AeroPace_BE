package com.group1.shop_runner.dto.order.response;

import com.group1.shop_runner.enums.CancelType;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderListResponse {
    private String orderCode;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String shippingAddress;
    private String phoneNumber;
    private String receiverName;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private CancelType cancelType;
    private String cancelNote;
    private PaymentStatus paymentStatus;
}