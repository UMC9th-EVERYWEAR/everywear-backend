package com.umc.EveryWear.domain.fitting.entity;

import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fitting_history")
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
    @JoinColumn(name = "user_product_id", nullable = false)
    private UserProduct userProduct;

    @Column(name = "fitting_result_image")
    private String fittingResultImage;

    public void applyFittingResult(String imageUrl) {
        this.fittingResultImage = imageUrl;
    }
}

