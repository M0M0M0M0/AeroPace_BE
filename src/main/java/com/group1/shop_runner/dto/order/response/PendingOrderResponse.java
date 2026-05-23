package com.group1.shop_runner.dto.order.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PendingOrderResponse {
    private Long orderId;
    private String paymentOrderId;
}
