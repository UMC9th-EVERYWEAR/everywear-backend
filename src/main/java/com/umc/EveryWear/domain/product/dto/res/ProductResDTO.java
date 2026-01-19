package com.umc.EveryWear.domain.product.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

        // 업데이트 여부 및 URL 업데이트 여부는 응답 포함X
        @JsonIgnore
        private Boolean isUpdated;
        @JsonIgnore
        private Boolean isUrlUpdated;
    }
}
