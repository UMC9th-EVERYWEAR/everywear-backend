package com.umc.EveryWear.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_url", nullable = false)
    private String productUrl;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column
    private String brand;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Float star;

    @Column(name = "product_img_url", nullable = false)
    private String productImgUrl;

    @Column(name = "AI_review", columnDefinition = "TEXT")
    private String aiReview;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
