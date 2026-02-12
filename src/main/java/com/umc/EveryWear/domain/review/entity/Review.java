package com.umc.EveryWear.domain.review.entity;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ========================================
    // 통일된 리뷰 필드 (7개)
    // ========================================

    @Column(name = "rating", nullable = false)
    private Integer rating;  // 별점 (1~5)

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;  // 리뷰 내용

    @Column(name = "review_date")
    private String reviewDate;  // 작성일 (사이트별 형식 그대로)

    @Column(name = "images", columnDefinition = "JSON")
    private String images;  // ["url1", "url2"] - JSON 배열 문자열

    @Column(name = "user_height")
    private Integer userHeight;  // 키 (cm)

    @Column(name = "user_weight")
    private Integer userWeight;  // 몸무게 (kg)

    @Column(name = "option_text", columnDefinition = "TEXT")
    private String optionText;  // 옵션 (사이트별 원본 그대로)
}