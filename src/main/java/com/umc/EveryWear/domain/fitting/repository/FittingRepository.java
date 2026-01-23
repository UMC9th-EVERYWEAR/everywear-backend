package com.umc.EveryWear.domain.fitting.repository;

import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FittingRepository extends JpaRepository<FittingHistory, Long> {
    
    // 사용자별 상품 조회 (updatedAt 내림차순)
    @Query("SELECT fh.product FROM FittingHistory fh WHERE fh.user.userId = :userId ORDER BY fh.updatedAt DESC")
    List<com.umc.EveryWear.domain.product.entity.Product> findAllProductsByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);
    
    // 사용자별 카테고리별 상품 조회 (updatedAt 내림차순)
    @Query("SELECT fh.product FROM FittingHistory fh WHERE fh.user.userId = :userId AND fh.product.category = :category ORDER BY fh.updatedAt DESC")
    List<com.umc.EveryWear.domain.product.entity.Product> findProductsByUserIdAndCategoryOrderByUpdatedAtDesc(@Param("userId") Long userId, @Param("category") String category);
    
    // 사용자별 상품 조회 상위 6개 (updatedAt 내림차순)
    @Query("SELECT fh.product FROM FittingHistory fh WHERE fh.user.userId = :userId ORDER BY fh.updatedAt DESC")
    List<com.umc.EveryWear.domain.product.entity.Product> findTop6ProductsByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    // 특정 사용자와 상품으로 FittingHistory 조회
    Optional<FittingHistory> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
    
    // updatedAt을 현재 시간으로 업데이트
    @Modifying
    @Query("UPDATE FittingHistory fh SET fh.updatedAt = :updatedAt WHERE fh.user.userId = :userId AND fh.product.productId = :productId")
    void updateUpdatedAt(@Param("userId") Long userId, @Param("productId") Long productId, @Param("updatedAt") LocalDateTime updatedAt);
}
