package com.group1.aeropace.controller;

import com.group1.aeropace.dto.StripeVerifyResult;
import com.group1.aeropace.dto.order.request.CheckoutRequest;
import com.group1.aeropace.entity.Order;
import com.group1.aeropace.service.OrderService;
import com.group1.aeropace.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.RoundingMode;
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
            // Tạo PaymentIntent trên Stripe (USD, đơn vị cents — nhân 100)
            long amountCents = request.getGrandTotal()
                    .setScale(2, RoundingMode.HALF_UP)
                    .movePointRight(2)
                    .longValueExact();
            StripeVerifyResult result = stripeService.createPaymentIntent(amountCents, "usd");

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
