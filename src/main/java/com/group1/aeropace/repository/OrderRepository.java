package com.group1.aeropace.repository;


import com.group1.aeropace.entity.Order;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.enums.PaymentMethod;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        SELECT o.user.id, COUNT(o.id), SUM(o.totalPrice)
        FROM Order o
        WHERE o.status = :status
          AND o.createdAt BETWEEN :from AND :to
        GROUP BY o.user.id
        ORDER BY SUM(o.totalPrice) DESC
        """)
    List<Object[]> findTopBuyersBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}