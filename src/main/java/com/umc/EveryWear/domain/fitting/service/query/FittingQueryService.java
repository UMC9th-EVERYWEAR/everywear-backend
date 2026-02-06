package com.umc.EveryWear.domain.fitting.service.query;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.exception.FittingException;
import com.umc.EveryWear.domain.fitting.exception.code.FittingErrorCode;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
import com.umc.EveryWear.domain.user.entity.UserImg;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.service.query.UserImgQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FittingQueryService {

    private final FittingHistoryRepository fittingHistoryRepository;
    private final UserImgQueryService userImgQueryService;

    /**
     * 내 피팅 목록 조회
     */
    public List<FittingResponseDto.FittingSummary> getMyFittings(Long userId) {
        return fittingHistoryRepository.
                findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * 피팅 상세 조회
     */
    public FittingResponseDto.FittingDetail getFittingDetail(
            Long userId,
            Long fittingId
    ) {
        FittingHistory history = fittingHistoryRepository
                .findByFittingIdAndUserId(fittingId, userId)
                .orElseThrow(() ->
                        new FittingException(FittingErrorCode.FITTING_HISTORY_NOT_FOUND)
                );

        UserProduct userProduct = history.getUserProduct();

        // BEFORE 이미지 (대표 프로필)
        UserImg representative = userImgQueryService.getRepresentativeImage(userId);

        return new FittingResponseDto.FittingDetail(
                history.getFittingId(),
                representative.getImageUrl(),          // BEFORE
                history.getFittingResultImage(),       // AFTER
                history.getCreatedAt(),
                new FittingResponseDto.ProductSummary(
                        userProduct.getProduct().getProductId(),
                        userProduct.getProduct().getShoppingmallName(),
                        userProduct.getProduct().getProductName(),
                        userProduct.getProduct().getPrice(),
                        userProduct.getProduct().getStarPoint(),
                        userProduct.getProduct().getProductUrl(),
                        userProduct.getIsLiked()
                )
        );
    }

    private FittingResponseDto.FittingSummary toSummary(FittingHistory h) {
        UserProduct up = h.getUserProduct();

        return new FittingResponseDto.FittingSummary(
                h.getFittingId(),
                h.getFittingResultImage(),
                h.getCreatedAt(),
                new FittingResponseDto.ProductBrief(
                        up.getProduct().getProductId(),
                        up.getProduct().getProductName(),
                        up.getIsLiked()
                )
        );
    }
}