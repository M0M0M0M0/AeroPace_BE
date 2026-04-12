package com.group1.shop_runner.repository;


import com.group1.shop_runner.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer>,
        JpaSpecificationExecutor<Order> {
    List<Order> findByUserId(Long userId);
}