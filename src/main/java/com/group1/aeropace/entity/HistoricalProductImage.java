package com.group1.aeropace.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "historical_product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historical_product_id", nullable = false)
    private HistoricalProduct historicalProduct;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "position", nullable = false)
    private Integer position;
}
