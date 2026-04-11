package com.group1.shop_runner.dto.cart.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MergeCartRequest {
    private Long userId;
    private String sessionId;
}
