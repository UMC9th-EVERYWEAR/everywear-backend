package com.umc.EveryWear.domain.product.entity;

import com.umc.EveryWear.domain.product.enums.ProductCrawlStatus;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_crawl_job")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductCrawlJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_url", nullable = false)
    private String productUrl;

    @Column(name = "shoppingmall_name", nullable = false)
    private String shoppingmallName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProductCrawlStatus status = ProductCrawlStatus.PROCESSING;

    @Column(name = "product_id")
    private Long productId;

    public void complete(Long productId) {
        this.productId = productId;
        this.status = ProductCrawlStatus.COMPLETED;
    }

    public void fail() {
        this.status = ProductCrawlStatus.FAILED;
    }
}
