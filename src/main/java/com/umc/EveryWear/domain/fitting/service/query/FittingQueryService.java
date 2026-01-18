package com.umc.EveryWear.domain.fitting.service.query;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.exception.FittingException;
import com.umc.EveryWear.domain.fitting.exception.code.FittingErrorCode;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
import com.umc.EveryWear.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FittingQueryService {

    private final FittingHistoryRepository fittingHistoryRepository;

    /**
     * 내 피팅 목록 조회 (최신순)
     */
    public List<FittingResponseDto.FittingSummary> getMyFittings(User user) {
        return fittingHistoryRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * 좋아요한 피팅 목록 조회 (최신순)
     */
    public List<FittingResponseDto.FittingSummary> getLikedFittings(User user) {
        return fittingHistoryRepository
                .findByUserAndIsLikedTrueOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * 피팅 상세 조회
     */
    public FittingResponseDto.FittingDetail getFittingDetail(
            User user,
            Long fittingId
    ) {
        FittingHistory history = fittingHistoryRepository
                .findByFittingIdAndUser(fittingId, user)
                .orElseThrow(() ->
                        new FittingException(FittingErrorCode.FITTING_HISTORY_NOT_FOUND)
                );

        return new FittingResponseDto.FittingDetail(
                history.getFittingId(),
                history.getFittingResultImage(),
                history.getIsLiked(),
                history.getProduct().getProductName(),
                history.getProduct().getCategory(),
                history.getCreatedAt()
        );
    }

    private FittingResponseDto.FittingSummary toSummary(FittingHistory h) {
        return new FittingResponseDto.FittingSummary(
                h.getFittingId(),
                h.getFittingResultImage(),
                h.getIsLiked(),
                h.getCreatedAt()
        );
    }
}