package com.group1.shop_runner.dto.order.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CheckoutRequest {
    private Long userId;
    private String shippingAddress;
    private String phoneNumber;
    private String paymentMethod;
    private String paymentOrderId;
    private String receiverName;
    private String note;
    private String ward;
    private String district;
    private String province;
    private BigDecimal vat;
    private Long shippingMethodId;
}