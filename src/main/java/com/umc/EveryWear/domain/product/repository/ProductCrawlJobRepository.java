package com.umc.EveryWear.domain.product.repository;

import com.umc.EveryWear.domain.product.entity.ProductCrawlJob;
import com.umc.EveryWear.domain.product.enums.ProductCrawlStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCrawlJobRepository extends JpaRepository<ProductCrawlJob, Long> {

    Optional<ProductCrawlJob> findByUserIdAndProductUrlAndStatus(
            Long userId, String productUrl, ProductCrawlStatus status);
}
