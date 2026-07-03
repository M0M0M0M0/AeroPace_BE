package com.group1.aeropace.controller;

import com.group1.aeropace.dto.order.response.OrderDetailResponse;
import com.group1.aeropace.dto.refund.request.RefundRequest;
import com.group1.aeropace.dto.refund.response.RefundResponse;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.service.OrderService;
import com.group1.aeropace.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private RefundService refundService;

    @GetMapping
    public Object getAllOrders(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        // page không được truyền (vd. Dashboard, thống kê nhanh) → trả về toàn bộ danh sách như cũ.
        if (page == null) {
            return orderService.getAllOrders(
                    orderCode, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo, userId
            );
        }
        return orderService.getAllOrdersPaged(
                orderCode, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo, userId,
                page, size != null ? size : 20
        );
    }

    @PutMapping("/{orderCode}/status")
    public void updateOrderStatus(
            @PathVariable String orderCode,
            @RequestParam OrderStatus status,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        orderService.updateOrderStatus(orderCode, status, reason);
    }

    @GetMapping("/details/{orderCode}")
    public OrderDetailResponse getOrderDetail(@PathVariable String orderCode){
        return orderService.getOrderDetail(orderCode);
    }

    @PostMapping("/{orderCode}/refund")
    public RefundResponse refund(@PathVariable String orderCode, @RequestBody RefundRequest request){
        return refundService.refund(orderCode, request);
    }
}
