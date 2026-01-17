package com.umc.EveryWear.domain.product.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ProductReqDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "무신사 상품 등록 요청")
    public static class ImportMusinsaDTO {
        @NotBlank(message = "상품 URL은 필수입니다.")
        @Pattern(regexp = "^(https://www\\.musinsa\\.com/products/\\d+$|https://musinsa\\.onelink\\.me/[^/]+/[^/]+.*)$", message = "지원되지 않는 url 형식입니다")
        @Schema(description = "무신사 상품 URL", example = "https://www.musinsa.com/products/5797789")
        @JsonProperty("product_url")
        private String product_url;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Import29cmDTO {
        @NotBlank(message = "상품 URL은 필수입니다.")
        @Pattern(regexp = "^(https://www\\.29cm\\.co\\.kr/products/\\d+.*|https://29cm\\.onelink\\.me/.*)$", message = "지원되지 않는 url 형식입니다")
        @Schema(description = "29cm 상품 URL", example = "https://www.29cm.co.kr/products/3401549?categoryLargeCode=268100100&categoryMediumCode=268105100&categorySmallCode=")
        @JsonProperty("product_url")
        private String product_url;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WconceptImportDTO {
        @NotBlank(message = "상품 URL은 필수입니다.")
        @Pattern(regexp = "^(https://www\\.wconcept\\.co\\.kr/Product/\\d+\\?.*|https://m\\.wconcept\\.co\\.kr/Product/\\d+\\?.*)$", message = "지원되지 않는 url 형식입니다")
        @Schema(description = "W컨셉 상품 URL", example = "https://www.wconcept.co.kr/Product/306170405?entry_channel=all_category&cate_no=001006&cate_nm=%ED%8C%AC%EC%B8%A0&cate_sort=")
        @JsonProperty("product_url")
        private String product_url;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportZigzagDTO {
        @NotBlank(message = "상품 URL은 필수입니다.")
        @Pattern(regexp = "^(https://zigzag\\.kr/catalog/products/\\d+$|https://s\\.zigzag\\.kr/[A-Za-z0-9]+$)", message = "지원되지 않는 url 형식입니다")
        @Schema(description = "지그재그 상품 URL", example = "https://zigzag.kr/catalog/products/135012008")
        @JsonProperty("product_url")
        private String product_url;
    }
}
