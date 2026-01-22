package com.umc.EveryWear.domain.review.repository;

import com.umc.EveryWear.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct_ProductId(Long productId);
    boolean existsByProduct_ProductId(Long productId);
}