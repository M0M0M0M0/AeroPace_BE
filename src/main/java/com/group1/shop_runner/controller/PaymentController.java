package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.StripeVerifyResult;
import com.group1.shop_runner.dto.order.request.CheckoutRequest;
import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.service.OrderService;
import com.group1.shop_runner.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class PaymentController {

    private final StripeService stripeService;
    @Autowired
    private OrderService orderService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(
            @RequestBody CheckoutRequest request) {
        try {
            // Tạo PaymentIntent trên Stripe
            StripeVerifyResult result = stripeService.createPaymentIntent(
                    Long.parseLong(request.getGrandTotal().toString()), "vnd");

            //  tạo order PENDING
            request.setPaymentOrderId(result.getPaymentIntentId());
            request.setPaymentMethod("stripe");
            Order order = orderService.checkout(request);

            return ResponseEntity.ok(Map.of(
                    "clientSecret", result.getClientSecret(),
                    "orderId", order.getId()
            ));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
