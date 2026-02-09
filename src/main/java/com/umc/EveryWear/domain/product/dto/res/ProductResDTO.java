package com.umc.EveryWear.domain.product.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.umc.EveryWear.domain.product.enums.ShoppingMall;
import lombok.Builder;
import lombok.Getter;

public class ProductResDTO {

    @Getter
    @Builder
    public static class ImportDTO {
        private Long product_id;
        private String shoppingmale_name;
        private String product_url;
        private String category;
        private String product_img_url;
        private String product_name;
        private String brand_name;
        private String price;
        private Float star_point;
        private String AI_review;
        private Long product_num;
        private Boolean is_liked;

        // 업데이트 여부 및 URL 업데이트 여부는 응답 포함X
        @JsonIgnore
        private Boolean isUpdated;
        @JsonIgnore
        private Boolean isUrlUpdated;
    }
    
    // 상품 등록/크롤링 응답. completed 시 product 채움, processing 시 job_id/estimated_time
    @Getter
    @Builder
    @JsonPropertyOrder({"product", "estimated_time", "from_cache", "job_id", "status"})
    public static class ImportResult {
        private ImportDTO product;
        private String estimated_time;
        private Boolean from_cache;
        private Long job_id;
        private String status;
        @JsonIgnore
        private ShoppingMall mall;
    }

    @Getter
    @Builder
    public static class LikeToggleDTO {
        private Boolean is_liked;
    }

    @Getter
    @Builder
    public static class ListDTO {
        private Long product_id;
        private String shoppingmale_name;
        private String product_url;
        private String category;
        private String product_img_url;
        private String product_name;
        private String brand_name;
        private String price;
        private Float star_point;
        private String AI_review;
        private Long product_num;
        private Boolean is_liked;
    }

    @Getter
    @Builder
    public static class ProductListResponse {
        private java.util.List<ListDTO> products;
    }

    // 피팅용 상품 상세 (상품 정보 + 대표 이미지)
    @Getter
    @Builder
    public static class ProductForFittingDTO {
        private String AI_review;
        private String brand_name;
        private String category;
        private Boolean is_liked;
        private String price;
        private Long product_id;
        private String product_img_url;
        private String product_name;
        private Long product_num;
        private String product_url;
        private String shoppingmale_name;
        private Float star_point;
    }

    // 상품 단건 조회 응답
    @Getter
    @Builder
    public static class ProductDetailResponse {
        private ProductForFittingDTO product;
    }
}
