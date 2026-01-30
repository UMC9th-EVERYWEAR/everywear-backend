package com.umc.EveryWear.domain.fitting.repository;

import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FittingHistoryRepository extends JpaRepository<FittingHistory, Long> {

    /**
     * 내 피팅 목록 조회 (최신순)
     */
    @Query("""
        select fh
        from FittingHistory fh
        join fh.userProduct up
        where up.user.userId = :userId
        order by fh.createdAt desc
    """)
    List<FittingHistory> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 피팅 상세 조회 (소유자 검증 포함)
     */
    @Query("""
        select fh
        from FittingHistory fh
        join fh.userProduct up
        where fh.fittingId = :fittingId
          and up.user.userId = :userId
    """)
    Optional<FittingHistory> findByFittingIdAndUserId(
            @Param("fittingId") Long fittingId,
            @Param("userId") Long userId
    );

}
