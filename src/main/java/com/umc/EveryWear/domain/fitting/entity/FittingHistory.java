package com.umc.EveryWear.domain.fitting.entity;

import com.umc.EveryWear.domain.fitting.enums.FittingStatus;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
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
    @JoinColumn(name = "user_product_id", nullable = false)
    private UserProduct userProduct;

    @Column(name = "fitting_result_image")
    private String fittingResultImage;

    @Enumerated(EnumType.STRING)
    private FittingStatus status;

    private String failReason;

    public void markProcessing() {
        this.status = FittingStatus.PROCESSING;
    }

    public void complete(String imageUrl) {
        this.fittingResultImage = imageUrl;
        this.status = FittingStatus.COMPLETED;
    }

    public void fail(String reason) {
        this.failReason = reason;
        this.status = FittingStatus.FAILED;
    }

    public void applyFittingResult(String imageUrl) {
        this.fittingResultImage = imageUrl;
    }
}

