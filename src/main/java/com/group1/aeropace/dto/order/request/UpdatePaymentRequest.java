package com.group1.aeropace.dto.order.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UpdatePaymentRequest {
    private String paymentOrderId;
    private String paymentTransactionId;
    private String paymentStatus;
}