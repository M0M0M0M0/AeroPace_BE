package com.group1.aeropace.event;

import com.group1.aeropace.entity.Order;
import com.group1.aeropace.entity.OrderItem;
import com.group1.aeropace.repository.OrderItemRepository;
import com.group1.aeropace.repository.OrderRepository;
import com.group1.aeropace.repository.UserRepository;
import com.group1.aeropace.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        Order order = event.getOrder();

        Order freshOrder = orderRepository.findById(order.getId())
                .orElseGet(() -> {
                    log.warn("[Event] Không tìm thấy order id={}", order.getId());
                    return null;
                });

        if (freshOrder == null) return;

        userRepository.findById(freshOrder.getUser().getId()).ifPresentOrElse(
                fullUser -> {
                    freshOrder.setUser(fullUser);

                    List<OrderItem> items = orderItemRepository.findByOrderId(freshOrder.getId());
                    freshOrder.setOrderItems(items);

                    emailService.sendOrderConfirmationEmail(freshOrder);
                },
                () -> log.warn("[Event] Không tìm thấy user id={} để gửi email đơn {}",
                        freshOrder.getUser().getId(), freshOrder.getOrderCode())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRefundCompleted(RefundCompletedEvent event) {
        Order order = event.getOrder();

        Order freshOrder = orderRepository.findById(order.getId())
                .orElseGet(() -> {
                    log.warn("[Event] Không tìm thấy order id={}", order.getId());
                    return null;
                });

        if (freshOrder == null) return;

        userRepository.findById(freshOrder.getUser().getId()).ifPresentOrElse(
                fullUser -> {
                    freshOrder.setUser(fullUser);
                    emailService.sendRefundConfirmationEmail(freshOrder);
                },
                () -> log.warn("[Event] Không tìm thấy user id={} để gửi email refund đơn {}",
                        freshOrder.getUser().getId(), freshOrder.getOrderCode())
        );
    }
}