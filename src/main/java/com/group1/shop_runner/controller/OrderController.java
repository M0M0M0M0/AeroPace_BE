package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.order.request.CheckoutRequest;
import com.group1.shop_runner.dto.order.request.UpdatePaymentRequest;
import com.group1.shop_runner.dto.order.response.OrderDetailResponse;
import com.group1.shop_runner.dto.order.response.OrderListResponse;
import com.group1.shop_runner.dto.order.response.PendingOrderResponse;
import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

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
    public Order checkout(@RequestBody CheckoutRequest request) {
        return orderService.checkout(request);
    }

    // =========================================================
    // API 2: Lấy danh sách đơn hàng theo userId
    // GET /api/orders/user/{userId}
    // Mục đích:
    // - Hiển thị lịch sử mua hàng của user
    // =========================================================
    @GetMapping("/user/{userId}")
    public List<OrderListResponse> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    // =========================================================
    // API 3: Lấy chi tiết 1 đơn hàng
    // GET /api/orders/{orderId}
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
    // PUT /api/orders/{orderId}/cancel
    // =========================================================
    @PutMapping("/{orderCode}/cancel")
    public void cancelOrder(@PathVariable String orderCode, Authentication authentication, String cancelNote) {
        orderService.cancelOrder(orderCode, authentication, cancelNote);
    }
    // =========================================================
    // API 6: Update Order payment status
    // PUT /api/orders/{orderId}/payment
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
    // PUT /api/orders/{orderId}/payment
    // =========================================================
    @GetMapping("/paypal/pending/{userId}")
    public List<PendingOrderResponse> getUserPendingOrder(@PathVariable Long userId){
        return orderService.getUserPendingOrder(userId);
    }

}