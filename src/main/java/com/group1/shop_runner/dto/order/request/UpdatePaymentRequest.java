package com.group1.shop_runner.dto.order.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePaymentRequest {
    private String paymentOrderId;
    private String paymentTransactionId;
    private String paymentStatus;
}