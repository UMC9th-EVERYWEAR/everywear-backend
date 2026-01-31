package com.umc.EveryWear.domain.review.repository;

import com.umc.EveryWear.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct_ProductId(Long productId);
    boolean existsByProduct_ProductId(Long productId);

    /**
     * 특정 상품의 리뷰 내용만 조회 (AI 요약용)
     */
    @Query("SELECT r.content FROM Review r WHERE r.product.productId = :productId")
    List<String> findReviewContentsByProductId(@Param("productId") Long productId);
}