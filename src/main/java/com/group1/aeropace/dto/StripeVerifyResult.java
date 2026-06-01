package com.group1.aeropace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StripeVerifyResult {
    private String paymentIntentId;
    private String clientSecret;
}
