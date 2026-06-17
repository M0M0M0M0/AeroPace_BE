package com.group1.aeropace.controller;

import com.group1.aeropace.dto.order.response.OrderDetailResponse;
import com.group1.aeropace.dto.order.response.OrderListResponse;
import com.group1.aeropace.dto.refund.request.RefundRequest;
import com.group1.aeropace.dto.refund.response.RefundResponse;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.service.OrderService;
import com.group1.aeropace.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long userId
    ) {
        return orderService.getAllOrders(
                orderCode, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo, userId
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
