package com.group1.aeropace.controller;

import com.group1.aeropace.dto.order.request.CheckoutRequest;
import com.group1.aeropace.dto.order.request.UpdatePaymentRequest;
import com.group1.aeropace.dto.order.response.OrderListResponse;
import com.group1.aeropace.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request) {
        var order = orderService.checkout(request);
        return ResponseEntity.ok(Map.of("id", order.getId()));
    }

    @GetMapping("/user/{userId}")
    public List<OrderListResponse> getOrdersByUserId(@PathVariable Long userId, Authentication authentication) {
        return orderService.getOrdersByUserId(userId, authentication);
    }

    @PutMapping("/{orderCode}/cancel")
    public void cancelOrder(@PathVariable String orderCode, Authentication authentication,
                            @RequestParam(required = false) String cancelNote) {
        orderService.cancelOrder(orderCode, authentication, cancelNote);
    }

    @PatchMapping("/{orderId}/payment")
    public ResponseEntity<Void> updatePayment(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentRequest request
    ) {
        orderService.updatePayment(orderId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderCode}/confirm")
    public ResponseEntity<Void> confirmDelivered(
            @PathVariable String orderCode,
            Authentication authentication) {
        orderService.confirmDelivered(orderCode, authentication);
        return ResponseEntity.ok().build();
    }
}
