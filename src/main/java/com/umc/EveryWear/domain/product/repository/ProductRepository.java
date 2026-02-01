package com.umc.EveryWear.domain.product.repository;

import com.umc.EveryWear.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 전역 상품 조회 (user_id 무관)
    // 상품 등록 여부판단에 사용
    Optional<Product> findByProductUrl(String productUrl);
    Optional<Product> findByProductNum(Long productNum);
    Optional<Product> findByProductName(String productName);

    // updatedAt 내림차순으로 모든 상품 조회
    List<Product> findAllByOrderByUpdatedAtDesc();

    // updatedAt을 현재 시간으로 업데이트
    @Modifying
    @Query("UPDATE Product p SET p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :productId")
    void updateUpdatedAt(@Param("productId") Long productId);

    // 상품 URL과 updatedAt을 업데이트
    @Modifying
    @Query("UPDATE Product p SET p.productUrl = :productUrl, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :productId")
    void updateProductUrlAndUpdatedAt(@Param("productId") Long productId, @Param("productUrl") String productUrl);

    // AI 리뷰 업데이트
    @Modifying
    @Query("UPDATE Product p SET p.aiReview = :aiReview, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :productId")
    void updateAiReview(@Param("productId") Long productId, @Param("aiReview") String aiReview);
}
