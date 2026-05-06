package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.order.request.CheckoutRequest;
import com.group1.shop_runner.dto.order.response.OrderDetailResponse;
import com.group1.shop_runner.dto.order.response.OrderListResponse;
import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
//@CrossOrigin(origins = "*")
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
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderById(@PathVariable Integer orderId) {
        return orderService.getOrderById(orderId);
    }
    // =========================================================
    // API 4: Update Order Status
    // PUT /api/orders/{orderId}/status
    // =========================================================
    @PutMapping("/{orderId}/status")
    public void updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam OrderStatus status
    ) {
        orderService.updateOrderStatus(orderId, status);
    }
    // =========================================================
    // API 5: Cancel Order
    // PUT /api/orders/{orderId}/cancel
    // =========================================================
    @PutMapping("/{orderId}/cancel")
    public void cancelOrder(@PathVariable Integer orderId) {
        orderService.cancelOrder(orderId);
    }
    // API 6: Lấy tất cả đơn hàng (Admin)
    // GET /api/v1/orders
    @GetMapping
    public List<OrderListResponse> getAllOrders(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) {
        return orderService.getAllOrders(
                id, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo
        );
    }
}