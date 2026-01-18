package com.umc.EveryWear.domain.fitting.repository;

import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FittingHistoryRepository extends JpaRepository<FittingHistory, Integer> {

    // 내 피팅 목록 (최신순)
    List<FittingHistory> findByUserOrderByCreatedAtDesc(User user);

    // 좋아요한 피팅 목록 (최신순)
    List<FittingHistory> findByUserAndIsLikedTrueOrderByCreatedAtDesc(User user);

    // 피팅 상세
    Optional<FittingHistory> findByFittingIdAndUser(
            Long fittingId,
            User user
    );

}
