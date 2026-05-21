package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.order.response.OrderDetailResponse;
import com.group1.shop_runner.dto.order.response.OrderListResponse;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

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
    //update order status
    @PutMapping("/{orderId}/status")
    public void updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        orderService.updateOrderStatus(orderId, status, reason);
    }
    //get order detail
    @GetMapping("/details/{id}")
    public OrderDetailResponse getOrderDetail(@PathVariable Long id){
        return orderService.getOrderDetail(id);
    }
}
