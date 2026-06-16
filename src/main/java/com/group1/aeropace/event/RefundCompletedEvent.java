package com.group1.aeropace.event;

import com.group1.aeropace.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RefundCompletedEvent extends ApplicationEvent {

    private final Order order;

    public RefundCompletedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
