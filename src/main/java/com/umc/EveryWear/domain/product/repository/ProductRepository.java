package com.umc.EveryWear.domain.product.repository;

import com.umc.EveryWear.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductNum(Long productNum);

    @Modifying
    @Query("UPDATE Product p SET p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :productId")
    void updateUpdatedAt(@Param("productId") Long productId);

    // AI 리뷰 업데이트
    @Modifying
    @Query("UPDATE Product p SET p.aiReview = :aiReview, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :productId")
    void updateAiReview(@Param("productId") Long productId, @Param("aiReview") String aiReview);
}
