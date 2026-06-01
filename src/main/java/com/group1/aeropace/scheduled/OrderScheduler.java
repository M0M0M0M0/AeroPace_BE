package com.group1.aeropace.scheduled;

import com.group1.aeropace.entity.Order;
import com.group1.aeropace.enums.CancelType;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.enums.PaymentMethod;
import com.group1.aeropace.enums.PaymentStatus;
import com.group1.aeropace.repository.OrderRepository;
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
            order.setCancelType(CancelType.PAYMENT_TIMEOUT);
            order.setCancelReason("Hết thời gian thanh toán PayPal");
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            log.info("Auto cancelled expired PayPal order: {}", order.getId());
        });
    }
}
