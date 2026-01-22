package com.umc.EveryWear.domain.review.dto.res;

import com.umc.EveryWear.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

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
        private Float star_point;
        private String review_contact;
        private String review_date;
        private String user_height;
        private String user_weight;
        private String option_text;
        private List<String> images;

        public static ReviewDTO from(Review review) {
            return ReviewDTO.builder()
                    .review_id(review.getReviewId())
                    .star_point(review.getStarPoint())
                    .review_contact(review.getReviewContact())
                    .review_date(review.getReviewDate())
                    .user_height(review.getUserHeight())
                    .user_weight(review.getUserWeight())
                    .option_text(review.getOptionText())
                    .images(review.getReviewPhotos() != null ?
                            review.getReviewPhotos().stream()
                                    .map(photo -> photo.getPhotoUrl())
                                    .toList() : List.of())
                    .build();
        }
    }
}