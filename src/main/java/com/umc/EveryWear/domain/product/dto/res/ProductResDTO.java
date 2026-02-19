package com.umc.EveryWear.domain.product.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    @Getter
    @Builder
    public static class ImportResult {
        private ImportDTO dto;
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
