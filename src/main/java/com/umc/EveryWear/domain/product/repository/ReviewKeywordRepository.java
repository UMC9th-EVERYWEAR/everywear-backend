package com.umc.EveryWear.domain.product.repository;

import com.umc.EveryWear.domain.product.entity.ReviewKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Long> {

    /**
     * 특정 상품의 모든 키워드 조회
     */
    List<ReviewKeyword> findByProduct_ProductId(Long productId);

    /**
     * 특정 상품의 모든 키워드 삭제 (재생성 전 기존 키워드 삭제용)
     */
    @Modifying
    @Query("DELETE FROM ReviewKeyword rk WHERE rk.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}