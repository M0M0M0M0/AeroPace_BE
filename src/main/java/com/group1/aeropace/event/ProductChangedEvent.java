package com.group1.aeropace.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductChangedEvent extends ApplicationEvent {

    private final Long productId;

    public ProductChangedEvent(Object source, Long productId) {
        super(source);
        this.productId = productId;
    }
}
