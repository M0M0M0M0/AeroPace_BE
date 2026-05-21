package com.group1.shop_runner.repository;

import com.group1.shop_runner.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod,Long> {
}
