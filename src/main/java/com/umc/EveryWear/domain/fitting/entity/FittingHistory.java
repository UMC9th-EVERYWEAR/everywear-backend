package com.umc.EveryWear.domain.fitting.entity;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fittingHistory")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FittingHistory extends BaseEntity {

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
}

