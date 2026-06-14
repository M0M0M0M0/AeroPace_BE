package com.group1.aeropace.controller;

import com.group1.aeropace.dto.order.request.CheckoutRequest;
import com.group1.aeropace.dto.order.request.UpdatePaymentRequest;
import com.group1.aeropace.dto.order.response.OrderDetailResponse;
import com.group1.aeropace.dto.order.response.OrderListResponse;
import com.group1.aeropace.dto.order.response.PendingOrderResponse;
import com.group1.aeropace.entity.Order;
import com.group1.aeropace.service.OrderService;
import com.group1.aeropace.service.StripeService;
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

    @Autowired
    private StripeService stripeService;

    // =========================================================
    // API 1: Checkout
    // POST /api/orders/checkout
    // Mục đích:
    // - Tạo đơn hàng từ cart
    // - Tạo Order + OrderItem
    // - Tính totalPrice
    // - Xóa cart sau khi checkout thành công
    // =========================================================
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        Order order = orderService.checkout(request);
        return ResponseEntity.ok(Map.of("id", order.getId()));
    }

    // =========================================================
    // API 2: Lấy danh sách đơn hàng theo userId
    // GET /api/v1/orders/user/{userId}
    // Mục đích:
    // - Hiển thị lịch sử mua hàng của user
    // =========================================================
    @GetMapping("/user/{userId}")
    public List<OrderListResponse> getOrdersByUserId(@PathVariable Long userId, Authentication authentication) {
        return orderService.getOrdersByUserId(userId, authentication);
    }

    // =========================================================
    // API 3: Lấy chi tiết 1 đơn hàng
    // GET /api/v1/orders/{orderId}
    // Mục đích:
    // - Hiển thị chi tiết đơn hàng
    // - Bao gồm danh sách OrderItem
    // =========================================================
    @GetMapping("/{orderCode}")
    public OrderDetailResponse getOrderById(@PathVariable String orderCode) {
        return orderService.getOrderByCode(orderCode);
    }
    // =========================================================

    // =========================================================
    // API 5: Cancel Order
    // PUT /api/v1/orders/{orderId}/cancel
    // =========================================================
    @PutMapping("/{orderCode}/cancel")
    public void cancelOrder(@PathVariable String orderCode, Authentication authentication,
                            @RequestParam(required = false) String cancelNote) {
        orderService.cancelOrder(orderCode, authentication, cancelNote);
    }
    // =========================================================
    // API 6: Update Order payment status
    // PUT /api/v1/orders/{orderId}/payment
    // =========================================================
    @PatchMapping("/{orderId}/payment")
    public ResponseEntity<Void> updatePayment(
            @PathVariable Long orderId,
            @RequestBody UpdatePaymentRequest request
    ) {
        orderService.updatePayment(orderId, request);
        return ResponseEntity.ok().build();
    }
    // =========================================================
    // API 7: Get pending order
    // PUT /api/v1/orders/{orderId}/payment
    // =========================================================
    @GetMapping("/paypal/pending/{userId}")
    public List<PendingOrderResponse> getUserPendingOrder(@PathVariable Long userId){
        return orderService.getUserPendingOrder(userId);
    }
    // =========================================================
    // API 8: User confirm order
    // PATCH /api/v1/orders/{orderCode}/confirm
    // =========================================================
    @PatchMapping("/{orderCode}/confirm")
    public ResponseEntity<Void> confirmDelivered(
            @PathVariable String orderCode,
            Authentication authentication) {
        orderService.confirmDelivered(orderCode, authentication);
        return ResponseEntity.ok().build();
    }

}