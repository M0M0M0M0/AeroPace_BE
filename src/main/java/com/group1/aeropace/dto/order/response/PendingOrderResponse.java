package com.group1.aeropace.dto.order.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PendingOrderResponse {
    private Long orderId;
    private String paymentOrderId;
}
