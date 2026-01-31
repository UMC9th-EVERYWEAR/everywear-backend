package com.umc.EveryWear.domain.closet.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ClosetResDTO {

    @Getter
    @Builder
    public static class ProductDTO {
        @JsonProperty("AI_review")
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

    @Getter
    @Builder
    public static class ProductListResponse {
        private List<ProductDTO> products;
    }
}
