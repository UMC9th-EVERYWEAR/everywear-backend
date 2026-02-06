package com.umc.EveryWear.domain.user.repository;

import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
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
public interface UserProductRepository extends JpaRepository<UserProduct, Long> {

    // 사용자별 상품 전체 조회 (updatedAt 내림차순)
    @Query("SELECT up FROM UserProduct up WHERE up.user.userId = :userId ORDER BY up.updatedAt DESC")
    List<UserProduct> findAllByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    // 사용자별 좋아요한 상품 조회 (updatedAt 내림차순)
    @Query("SELECT up FROM UserProduct up WHERE up.user.userId = :userId AND up.isLiked = true ORDER BY up.updatedAt DESC")
    List<UserProduct> findLikedProductsByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    // 사용자별 좋아요한 상품 중 카테고리별 조회 (updatedAt 내림차순)
    @Query("SELECT up FROM UserProduct up WHERE up.user.userId = :userId AND up.isLiked = true AND up.product.category = :category ORDER BY up.updatedAt DESC")
    List<UserProduct> findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(@Param("userId") Long userId, @Param("category") String category);

    // 사용자별 카테고리별 상품 조회 (updatedAt 내림차순)
    @Query("SELECT up FROM UserProduct up WHERE up.user.userId = :userId AND up.product.category = :category ORDER BY up.updatedAt DESC")
    List<UserProduct> findAllByUserIdAndCategoryOrderByUpdatedAtDesc(@Param("userId") Long userId, @Param("category") String category);

    // 사용자별 상품 상위 N개 조회 (updatedAt 내림차순, Pageable로 개수 지정)
    @Query("SELECT up FROM UserProduct up WHERE up.user.userId = :userId ORDER BY up.updatedAt DESC")
    List<UserProduct> findTop6ByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 특정 사용자와 상품으로 UserProduct 조회
    Optional<UserProduct> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    // updatedAt을 현재 시간으로 업데이트
    @Modifying
    @Query("UPDATE UserProduct up SET up.updatedAt = :updatedAt WHERE up.user.userId = :userId AND up.product.productId = :productId")
    void updateUpdatedAt(@Param("userId") Long userId, @Param("productId") Long productId, @Param("updatedAt") LocalDateTime updatedAt);

    // 60일 경과된 UserProduct 조회
    @Query("SELECT up FROM UserProduct up WHERE up.updatedAt < :cutoffDate")
    List<UserProduct> findAllByUpdatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    boolean existsByUser_UserIdAndProduct_ProductIdAndIsLikedTrue(Long userId, Long productId);

    List<UserProduct> findAllByUser_UserId(Long userId);
    void deleteAllByUser_UserId(Long userId);
}
