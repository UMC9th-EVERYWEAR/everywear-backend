package com.umc.EveryWear.domain.product.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ProductReqDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportDTO {
        @NotBlank(message = "상품 URL은 필수입니다.")
        @Pattern(regexp = "^https://www\\.musinsa\\.com/products/\\d+$", message = "지원되지 않는 url 형식입니다")
        private String product_url;
    }
}
