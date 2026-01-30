package com.umc.EveryWear.domain.user.entity.mapping;

import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_product")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_product_id")
    private Long userProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "is_liked", nullable = false)
    @Builder.Default
    private Boolean isLiked = false;

    @OneToMany(mappedBy = "userProduct", cascade = CascadeType.ALL)
    @Builder.Default
    private List<FittingHistory> fittingHistories = new ArrayList<>();

    public void toggleLike() {
        this.isLiked = !Boolean.TRUE.equals(this.isLiked);
    }
}

