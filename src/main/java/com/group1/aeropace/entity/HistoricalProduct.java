package com.group1.aeropace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "historical_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "brand", length = 200)
    private String brand;

    @Column(name = "option1_name", length = 50)
    private String option1Name;

    @Column(name = "option2_name", length = 50)
    private String option2Name;

    @Column(name = "option3_name", length = 50)
    private String option3Name;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @OneToMany(mappedBy = "historicalProduct", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HistoricalProductImage> images;

    @OneToMany(mappedBy = "historicalProduct", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HistoricalProductVariant> variants;
}
