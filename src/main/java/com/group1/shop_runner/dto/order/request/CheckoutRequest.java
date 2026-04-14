package com.group1.shop_runner.dto.order.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private Long userId;
    private String shippingAddress;
    private String phoneNumber;
    private String paymentMethod;
    private String receiverName;
    private String note;
}