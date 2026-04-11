package com.group1.shop_runner.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductCategoryId implements Serializable {
    private Long productId;
    private Long categoryId;
}
