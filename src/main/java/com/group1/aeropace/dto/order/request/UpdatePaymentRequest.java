package com.group1.aeropace.dto.order.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UpdatePaymentRequest {
    @Size(max = 100, message = "Payment order id must not exceed 100 characters")
    private String paymentOrderId;

    @Size(max = 100, message = "Payment transaction id must not exceed 100 characters")
    private String paymentTransactionId;

    private String paymentStatus;
}