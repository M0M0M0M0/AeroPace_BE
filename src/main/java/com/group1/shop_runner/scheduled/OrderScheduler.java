package com.group1.shop_runner.scheduled;

import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.enums.CancelReason;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.enums.PaymentMethod;
import com.group1.shop_runner.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 60000)
    public void cancelExpiredPaypalOrders() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(150);

        List<Order> expiredOrders = orderRepository
                .findByStatusAndPaymentMethodAndCreatedAtBefore(
                        OrderStatus.PENDING,
                        PaymentMethod.PAYPAL,
                        expiredTime
                );

        expiredOrders.forEach(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelReason(CancelReason.PAYMENT_TIMEOUT);
            order.setCancelNote("Hết thời gian thanh toán PayPal");
            order.setPaymentStatus("UNPAID");
            orderRepository.save(order);
            log.info("Auto cancelled expired PayPal order: {}", order.getId());
        });
    }
}
