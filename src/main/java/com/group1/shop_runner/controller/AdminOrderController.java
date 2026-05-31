package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.order.response.OrderDetailResponse;
import com.group1.shop_runner.dto.order.response.OrderListResponse;
import com.group1.shop_runner.dto.refund.request.RefundRequest;
import com.group1.shop_runner.dto.refund.response.RefundResponse;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.service.OrderService;
import com.group1.shop_runner.service.RefundService;
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

    @Autowired
    private RefundService refundService;

    @GetMapping
    public List<OrderListResponse> getAllOrders(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) {
        return orderService.getAllOrders(
                orderCode, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo
        );
    }
    //update order status
    @PutMapping("/{orderCode}/status")
    public void updateOrderStatus(
            @PathVariable String orderCode,
            @RequestParam OrderStatus status,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        orderService.updateOrderStatus(orderCode, status, reason);
    }
    //get order detail
    @GetMapping("/details/{orderCode}")
    public OrderDetailResponse getOrderDetail(@PathVariable String orderCode){
        return orderService.getOrderDetail(orderCode);
    }
    //refund
    @PostMapping("/{orderCode}/refund")
    public RefundResponse refund(@PathVariable String orderCode, @RequestBody RefundRequest request){
        return refundService.refund(orderCode, request);
    }
}
