package com.umc.EveryWear.domain.fitting.service.command;

import com.umc.EveryWear.domain.fitting.client.GeminiImageClient;
import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
import com.umc.EveryWear.domain.fitting.service.command.strategy.FittingStrategy;
import com.umc.EveryWear.domain.fitting.service.command.strategy.FittingStrategyResolver;
import com.umc.EveryWear.domain.user.entity.UserImg;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.exception.UserProductException;
import com.umc.EveryWear.domain.user.exception.code.UserProductErrorCode;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import com.umc.EveryWear.domain.user.service.query.UserImgQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FittingCommandService {

    private final FittingStrategyResolver fittingStrategyResolver;
    private final UserProductRepository userProductRepository;
    private final FittingHistoryRepository fittingHistoryRepository;
    private final GeminiImageClient geminiImageClient;
    private final UserImgQueryService userImgQueryService;

    /**
     * 피팅 요청 + 히스토리 생성
     */
    public FittingResponseDto.FittingApplyResult requestFitting(
            Long userId,
            Long productId
    ) {
// 1. UserProduct 조회
        UserProduct userProduct = userProductRepository
                .findByUser_UserIdAndProduct_ProductId(userId, productId)
                .orElseThrow(() ->
                        new UserProductException(UserProductErrorCode.USER_PRODUCT_NOTFOUND)
                );

        // 2. 사용자 대표사진 조회 (BEFORE)
        UserImg userImg = userImgQueryService.getRepresentativeImage(userId);
        String userImageUrl = userImg.getImageUrl();

        // 3. 상품 이미지 URL
        String garmentImageUrl = userProduct.getProduct().getProductImgUrl();

        // 4. 카테고리 변환
        ClothingCategory category =
                ClothingCategory.from(userProduct.getProduct().getCategory());

        // 5. 전략 선택
        FittingStrategy strategy = fittingStrategyResolver.find(category);

        // 6. 피팅 히스토리 생성
        FittingHistory fittingHistory = FittingHistory.builder()
                .userProduct(userProduct)
                .build();

        fittingHistoryRepository.save(fittingHistory);

        // 7. AI 피팅 요청
        String resultImageUrl = geminiImageClient.generateFittingImage(
                userImageUrl,
                garmentImageUrl,
                strategy.buildPrompt()
        );

        // 8. 결과 반영
        fittingHistory.applyFittingResult(resultImageUrl);

        return new FittingResponseDto.FittingApplyResult(
                fittingHistory.getFittingId(),
                resultImageUrl
        );
    }
}
