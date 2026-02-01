package com.umc.EveryWear.domain.product.entity;

import com.umc.EveryWear.domain.product.enums.ReviewCrawlStatus;
import com.umc.EveryWear.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "shoppingmall_name", nullable = false)
    private String shoppingmallName;

    @Column(name = "product_url", nullable = false)
    private String productUrl;

    @Column(nullable = false)
    private String category;

    @Column(name = "product_img_url", nullable = false)
    private String productImgUrl;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(nullable = false)
    private String price;

    @Column(name = "star_point")
    private Float starPoint;

    @Column(name = "AI_review", columnDefinition = "TEXT")
    private String aiReview;

    @Column(name = "product_num")
    private Long productNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_crawl_status")
    private ReviewCrawlStatus reviewCrawlStatus;

    // 상품 URL 업데이트 메서드
    public void updateProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public void updateReviewCrawlStatus(ReviewCrawlStatus status) {
        this.reviewCrawlStatus = status;
    }
}
