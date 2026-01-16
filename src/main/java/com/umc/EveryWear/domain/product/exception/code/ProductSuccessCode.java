package com.umc.EveryWear.domain.product.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductSuccessCode implements BaseSuccessCode {

    MUSINSA_PRODUCT_ADDED(HttpStatus.OK,
            "200",
            "무신사 상품을 추가했습니다."),
    ZIGZAG_PRODUCT_ADDED(HttpStatus.OK,
            "200",
            "지그재그 상품을 추가했습니다."),
    WCONCEPT_PRODUCT_ADDED(HttpStatus.OK,
            "200",
            "W컨셉 상품을 추가했습니다."),
    CM29_PRODUCT_ADDED(HttpStatus.OK,
            "200",
            "29cm 상품을 추가했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
