package com.umc.EveryWear.domain.fitting.service.command;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import com.umc.EveryWear.domain.fitting.exception.FittingException;
import com.umc.EveryWear.domain.fitting.exception.code.FittingErrorCode;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
import com.umc.EveryWear.domain.fitting.service.command.strategy.FittingStrategy;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.UserImg;
import com.umc.EveryWear.domain.user.exception.UserImgException;
import com.umc.EveryWear.domain.user.exception.code.UserImgErrorCode;
import com.umc.EveryWear.domain.user.repository.UserImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FittingCommandService {

    private final UserImgRepository userImgRepository;
    private final ProductRepository productRepository;
    private final FittingHistoryRepository fittingHistoryRepository;
    private final List<FittingStrategy> fittingStrategies;

    /**
     * 가상 피팅 실행
     *
     * @param user      로그인 사용자
     * @param productId 피팅할 상품 ID
     * @param userImgId 검증된 사용자 이미지 ID
     */
    public FittingResponseDto.FittingApplyResult tryOn(
            User user,
            Long productId,
            Long userImgId
    ) {
        // 사용자 이미지 조회
        UserImg userImg = userImgRepository
                .findByProfileImageIdAndUser(userImgId, user)
                .orElseThrow(() ->
                        new UserImgException(UserImgErrorCode.USER_IMAGE_NOT_FOUND)
                );

        // String userImageUrl = userImg.getImageUrl();

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new FittingException(FittingErrorCode.PRODUCT_NOT_FOUND)
                );

        // 상품 카테고리 변환 (String → Enum, 기본값 OTHER)
        ClothingCategory category =
                ClothingCategory.from(product.getCategory());

        // 카테고리에 맞는 피팅 전략 선택
        FittingStrategy strategy = fittingStrategies.stream()
                .filter(s -> s.supports(category))
                .findFirst()
                .orElseThrow(() ->
                        new FittingException(FittingErrorCode.INVALID_PRODUCT_CATEGORY)
                );

        // (임시) 피팅 결과 생성, S3 업로드 로직 구현 예정
        // TODO: GeminiImageClient + S3 붙일 때 실제 이미지 생성 로직 추가
        String resultImageUrl = "TEMP_FITTING_RESULT_IMAGE";

        // 피팅 히스토리 저장
        FittingHistory history = fittingHistoryRepository.save(
                FittingHistory.builder()
                        .user(user)
                        .product(product)
                        .fittingResultImage(resultImageUrl)
                        .build()
        );

        // 응답
        return new FittingResponseDto.FittingApplyResult(
                history.getFittingId(),
                history.getFittingResultImage()
        );
    }

    /**
     * 피팅 좋아요 토글
     */
    public void toggleLike(User user, Long fittingId) {
        FittingHistory history = fittingHistoryRepository
                .findByFittingIdAndUser(fittingId, user)
                .orElseThrow(() ->
                        new FittingException(FittingErrorCode.FITTING_HISTORY_NOT_FOUND)
                );

        history.toggleLike();
    }
}
