package com.umc.EveryWear.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviewKeyword")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewKeyword {

    @Id
    @Column(name = "keyword_id", length = 255)
    private String keywordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "keyword_name", nullable = false, length = 255)
    private String keywordName;
}
