package com.group1.aeropace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "historical_product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historical_product_id", nullable = false)
    private HistoricalProduct historicalProduct;

    @Column(name = "original_variant_id")
    private Long originalVariantId;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "compare_price")
    private BigDecimal comparePrice;

    @Column(name = "option1_value", length = 50)
    private String option1Value;

    @Column(name = "option2_value", length = 50)
    private String option2Value;

    @Column(name = "option3_value", length = 50)
    private String option3Value;
}
