package com.group1.shop_runner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"product_id", "option1_value", "option2_value", "option3_value"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK product_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "option1_value", length = 50)
    private String option1Value;

    @Column(name = "option2_value", length = 50)
    private String option2Value;

    @Column(name = "option3_value", length = 50)
    private String option3Value;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "compare_price")
    private BigDecimal comparePrice;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 100)
    private String sku;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
