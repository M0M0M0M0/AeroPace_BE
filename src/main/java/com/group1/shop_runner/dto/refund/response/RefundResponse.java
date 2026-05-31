package com.group1.shop_runner.dto.refund.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundResponse {
    private boolean success;
    private String message;
}
