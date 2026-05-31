package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.refund.request.RefundRequest;
import com.group1.shop_runner.dto.refund.response.RefundResponse;
import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.enums.PaymentMethod;
import com.group1.shop_runner.enums.PaymentStatus;
import com.group1.shop_runner.repository.OrderRepository;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefundService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private StripeService stripeService;

    public RefundResponse refund(String orderCode, RefundRequest request) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.CANCELLED || order.getPaymentStatus() != PaymentStatus.REFUND_PENDING) {
            throw new AppException(ErrorCode.INVALID_REFUND_STATUS);
        }

        try {
            if (order.getPaymentMethod() == PaymentMethod.PAYPAL) {
                payPalService.refundPayPal(order.getPaymentTransactionId());
            } else {
                stripeService.refundStripeCharge(order.getPaymentTransactionId());
            }
        } catch (StripeException e) {
            return RefundResponse.builder()
                    .success(false)
                    .message("Refund failed: " + e.getMessage())
                    .build();
        }

        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setRefundReason(request.getRefundReason());
        orderRepository.save(order);

        return RefundResponse.builder()
                .success(true)
                .message("Refund successful")
                .build();
    }
}
