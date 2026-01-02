package com.umc.EveryWear.domain.fitting.entity;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fittingHistory")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FittingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fitting_id")
    private Long fittingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "fitting_result_image")
    private String fittingResultImage;

    @Column(name = "is_liked", nullable = false)
    @Builder.Default
    private Boolean isLiked = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

