package com.group1.aeropace.scheduled;

import com.group1.aeropace.entity.Order;
import com.group1.aeropace.entity.OrderItem;
import com.group1.aeropace.enums.CancelType;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.enums.PaymentMethod;
import com.group1.aeropace.enums.PaymentStatus;
import com.group1.aeropace.repository.OrderItemRepository;
import com.group1.aeropace.repository.OrderRepository;
import com.group1.aeropace.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;

    @Scheduled(fixedRate = 60000)
    @Transactional
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

            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                inventoryService.restoreStock(item.getProductVariant().getId(), item.getQuantity(), order.getId());
            }

            log.info("Đã tự động hủy đơn PayPal hết hạn: {}, hoàn stock cho {} item",
                    order.getId(), items.size());
        });
    }
}
