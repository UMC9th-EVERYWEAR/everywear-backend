package com.umc.EveryWear.domain.product.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ProductReqDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrawlingDTO {
        @NotBlank(message = "상품 URL은 필수입니다.")
        private String product_url;
    }
}
