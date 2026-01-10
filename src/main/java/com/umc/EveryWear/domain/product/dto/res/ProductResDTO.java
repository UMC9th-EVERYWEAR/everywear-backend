package com.umc.EveryWear.domain.product.dto.res;

import lombok.Builder;
import lombok.Getter;

public class ProductResDTO {

    @Getter
    @Builder
    public static class CrawlingDTO {
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
    }
}
