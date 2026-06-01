package com.group1.shop_runner.event;

import com.group1.shop_runner.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderConfirmedEvent extends ApplicationEvent {

    private final Order order;

    public OrderConfirmedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}