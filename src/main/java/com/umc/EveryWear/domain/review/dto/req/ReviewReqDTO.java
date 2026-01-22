package com.umc.EveryWear.domain.review.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReviewReqDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 크롤링 요청")
    public static class CrawlReviewDTO {
        @NotNull(message = "상품 ID는 필수입니다.")
        @Schema(description = "상품 ID", example = "123")
        private Long product_id;

        @NotBlank(message = "상품 URL은 필수입니다.")
        @Schema(description = "상품 URL", example = "https://www.musinsa.com/products/5432652")
        private String product_url;

        @NotBlank(message = "쇼핑몰 이름은 필수입니다.")
        @Schema(description = "쇼핑몰 이름", example = "무신사")
        private String shoppingmall_name;
    }
}