package com.umc.EveryWear.domain.review.dto.res;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.EveryWear.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ReviewResDTO {

    @Getter
    @Builder
    public static class CrawlResponseDTO {
        private String status;
        private String estimated_time;
        private Boolean from_cache;
        private Integer total_count;
        private List<ReviewDTO> reviews;
    }

    @Getter
    @Builder
    public static class ReviewListDTO {
        private String status;
        private Integer total_count;
        private List<ReviewDTO> reviews;
    }

    @Getter
    @Builder
    public static class ReviewDTO {
        private Long review_id;
        private Integer rating;  // Float -> Integer로 변경
        private String content;  // review_contact -> content로 변경
        private String review_date;
        private Integer user_height;  // String -> Integer로 변경
        private Integer user_weight;  // String -> Integer로 변경
        private String option_text;
        private List<String> images;

        private static final ObjectMapper objectMapper = new ObjectMapper();

        public static ReviewDTO from(Review review) {
            List<String> imageList = new ArrayList<>();

            // JSON 문자열을 List<String>으로 파싱
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                try {
                    imageList = objectMapper.readValue(
                            review.getImages(),
                            new TypeReference<List<String>>() {}
                    );
                } catch (JsonProcessingException e) {
                    // JSON 파싱 실패 시 빈 리스트 유지
                    imageList = new ArrayList<>();
                }
            }

            return ReviewDTO.builder()
                    .review_id(review.getReviewId())
                    .rating(review.getRating())  // rating 사용
                    .content(review.getContent())  // content 사용
                    .review_date(review.getReviewDate())
                    .user_height(review.getUserHeight())  // Integer 그대로 사용
                    .user_weight(review.getUserWeight())  // Integer 그대로 사용
                    .option_text(review.getOptionText())
                    .images(imageList)  // JSON 파싱된 리스트 사용
                    .build();
        }
    }
}