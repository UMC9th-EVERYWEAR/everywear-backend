package com.umc.EveryWear.domain.review.dto.res;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.EveryWear.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class ReviewResDTO {

    @Getter
    @Builder
    @Schema(description = "리뷰 목록 조회 응답 DTO")
    public static class ReviewListDTO {
        @Schema(description = "크롤링 상태", example = "completed")
        private String status;

        @Schema(description = "총 리뷰 개수", example = "15")
        private Integer total_count;

        @Schema(description = "리뷰 상세 목록")
        private List<ReviewDTO> reviews;
    }

    @Getter
    @Builder
    @Schema(description = "리뷰 상세 정보")
    public static class ReviewDTO {
        @Schema(description = "리뷰 고유 ID", example = "101")
        private Long review_id;

        @Schema(description = "평점 (1~5)", example = "5")
        private Integer rating;

        @Schema(description = "리뷰 내용", example = "사이즈가 딱 맞고 재질이 좋아요.")
        private String content;

        @Schema(description = "리뷰 작성일", example = "2025.05.20 or 7일전")
        private String review_date;

        @Schema(description = "사용자 키 (cm)", example = "175")
        private Integer user_height;

        @Schema(description = "사용자 몸무게 (kg)", example = "70")
        private Integer user_weight;

        @Schema(description = "구매 옵션 정보", example = "Black / L")
        private String option_text;

        @Schema(description = "리뷰 이미지 URL 리스트")
        private List<String> images;

        private static final ObjectMapper objectMapper = new ObjectMapper();

        public static ReviewDTO from(Review review) {
            List<String> imageList = new ArrayList<>();
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                try {
                    imageList = objectMapper.readValue(
                            review.getImages(),
                            new TypeReference<List<String>>() {}
                    );
                } catch (JsonProcessingException e) {
                    imageList = new ArrayList<>();
                }
            }

            return ReviewDTO.builder()
                    .review_id(review.getReviewId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .review_date(review.getReviewDate())
                    .user_height(review.getUserHeight())
                    .user_weight(review.getUserWeight())
                    .option_text(review.getOptionText())
                    .images(imageList)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AI 리뷰 요약 및 키워드 DTO")
    public static class AiReviewDTO {
        @Schema(description = "AI 생성 리뷰 요약", example = "전체적으로 평점이 높으며 배송이 빠르다는 평이 많습니다.")
        private String summary;

        @Schema(description = "추출된 키워드 (최대 4개)", example = "[\"가성비\", \"빠른배송\", \"재질만족\", \"정사이즈\"]")
        private List<String> keywords;

        @Schema(description = "처리 메시지", example = "성공적으로 요약되었습니다.")
        private String message;
    }
}