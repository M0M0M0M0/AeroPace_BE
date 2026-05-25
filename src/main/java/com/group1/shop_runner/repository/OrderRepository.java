package com.group1.shop_runner.repository;


import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatusAndPaymentMethodAndCreatedAtBefore(
            OrderStatus status,
            PaymentMethod paymentMethod,
            LocalDateTime time
    );
    Optional<Order> findByOrderCode(String orderCode);
}