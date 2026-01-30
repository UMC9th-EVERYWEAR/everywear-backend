package com.umc.EveryWear.domain.fitting.service.command;

import com.umc.EveryWear.domain.fitting.client.GeminiImageClient;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
import com.umc.EveryWear.domain.fitting.service.command.strategy.FittingStrategy;
import com.umc.EveryWear.domain.fitting.service.command.strategy.FittingStrategyResolver;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.exception.UserProductException;
import com.umc.EveryWear.domain.user.exception.code.UserProductErrorCode;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
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

    /**
     * 피팅 요청 + 히스토리 생성
     */
    public Long requestFitting(
            Long userId,
            Long productId,
            String userImageUrl,
            String garmentImageUrl
    ) {

        // 1. UserProduct 조회
        UserProduct userProduct = userProductRepository
                .findByUser_UserIdAndProduct_ProductId(userId, productId)
                .orElseThrow(() -> new UserProductException(UserProductErrorCode.USER_PRODUCT_NOTFOUND));

        // 2. Product에서 카테고리 String 가져오기
        String rawCategory = userProduct.getProduct().getCategory();

        // 3. String → Enum 변환
        ClothingCategory category = ClothingCategory.from(rawCategory);

        // 4. 카테고리에 맞는 전략 선택
        FittingStrategy strategy = fittingStrategyResolver.find(category);

        // 5. 프롬프트 생성
        String prompt = strategy.buildPrompt();

        // 6. 피팅 히스토리 먼저 생성 (결과는 아직 없음)
        FittingHistory fittingHistory = FittingHistory.builder()
                .userProduct(userProduct)
                .build();

        fittingHistoryRepository.save(fittingHistory);

        // 7. AI 피팅 요청
        String resultImageUrl = geminiImageClient.generateFittingImage(
                userImageUrl,
                garmentImageUrl,
                prompt
        );

        // 8. 결과 반영
        fittingHistory.applyFittingResult(resultImageUrl);

        return fittingHistory.getFittingId();
    }
}
