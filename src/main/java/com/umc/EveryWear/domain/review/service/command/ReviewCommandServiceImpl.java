package com.umc.EveryWear.domain.review.service.command;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.entity.ReviewKeyword;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.product.repository.ReviewKeywordRepository;
import com.umc.EveryWear.domain.review.dto.AiReviewResult;
import com.umc.EveryWear.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final OpenAiService openAiService;

    /**
     * 특정 상품의 리뷰를 AI로 요약하고 키워드를 추출하여 저장
     * @param productId 상품 ID
     * @return 생성된 AI 요약 리뷰 및 키워드
     */
    @Override
    public AiReviewResult generateAiReview(Long productId) {
        // 1. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));

        // 2. 해당 상품의 모든 리뷰 내용 조회
        List<String> reviewContents = reviewRepository.findReviewContentsByProductId(productId);

        if (reviewContents.isEmpty()) {
            log.info("상품 ID {}에 대한 리뷰가 없습니다.", productId);
            return AiReviewResult.builder()
                    .summary(null)
                    .keywords(List.of())
                    .build();
        }

        log.info("상품 ID {}의 리뷰 {}개를 AI로 요약 및 키워드 추출 중...", productId, reviewContents.size());

        // 3. OpenAI API를 통해 리뷰 요약 및 키워드 추출
        AiReviewResult aiResult = openAiService.summarizeReviewsWithKeywords(reviewContents);

        // 4. Product의 aiReview 필드 업데이트
        productRepository.updateAiReview(product.getProductId(), aiResult.getSummary());

        // 5. 기존 키워드 삭제
        reviewKeywordRepository.deleteByProductId(productId);

        // 6. 새로운 키워드 저장
        saveKeywords(product, aiResult.getKeywords());

        log.info("상품 ID {}의 AI 리뷰 및 키워드 생성 완료", productId);

        return aiResult;
    }


    /**
     * 키워드 저장
     */
    private void saveKeywords(Product product, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            log.warn("저장할 키워드가 없습니다. 상품 ID: {}", product.getProductId());
            return;
        }

        for (String keywordName : keywords) {
            if (keywordName != null && !keywordName.trim().isEmpty()) {
                ReviewKeyword keyword = ReviewKeyword.builder()
                        .product(product)
                        .keywordName(keywordName.trim())
                        .build();

                reviewKeywordRepository.save(keyword);
                log.debug("키워드 저장 완료: {} (상품 ID: {})", keywordName, product.getProductId());
            }
        }
    }
}