package com.umc.EveryWear.domain.product.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductSuccessCode implements BaseSuccessCode {

    MUSINSA_PRODUCT_ADDED(HttpStatus.OK,
            "COMMON201_1",
            "무신사 상품을 추가했습니다."),
    MUSINSA_PRODUCT_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "무신사 상품을 업데이트했습니다."),
    MUSINSA_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "무신사 상품의 URL을 업데이트했습니다."),
    ZIGZAG_PRODUCT_ADDED(HttpStatus.OK,
            "COMMON201_1",
            "지그재그 상품을 추가했습니다."),
    ZIGZAG_PRODUCT_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "지그재그 상품을 업데이트했습니다."),
    ZIGZAG_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "지그재그 상품의 URL을 업데이트했습니다."),
    WCONCEPT_PRODUCT_ADDED(HttpStatus.OK,
            "COMMON201_1",
            "W컨셉 상품을 추가했습니다."),
    WCONCEPT_PRODUCT_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "W컨셉 상품을 업데이트했습니다."),
    WCONCEPT_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "W컨셉 상품의 URL을 업데이트했습니다."),
    CM29_PRODUCT_ADDED(HttpStatus.OK,
            "COMMON201_1",
            "29cm 상품을 추가했습니다."),
    CM29_PRODUCT_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "29cm 상품을 업데이트했습니다."),
    CM29_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "COMMON202_1",
            "29cm 상품의 URL을 업데이트했습니다."),
    PRODUCTS_RETRIEVED(HttpStatus.OK,
            "COMMON200",
            "전체 상품을 조회했습니다."),
    TOP_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "COMMON200",
            "상의 상품을 조회했습니다."),
    BOTTOM_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "COMMON200",
            "하의 상품을 조회했습니다."),
    OUTER_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "COMMON200",
            "아우터 상품을 조회했습니다."),
    DRESS_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "COMMON200",
            "원피스 상품을 조회했습니다."),
    ETC_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "COMMON200",
            "기타 상품을 조회했습니다."),
    PRODUCTS_RETRIEVED_EMPTY(HttpStatus.OK,
            "COMMON204_1",
            "상품을 조회했습니다. 응답 본문이 없습니다."),
    PRODUCT_LIKE_ENABLED(HttpStatus.OK,
            "COMMON200",
            "상품 좋아요가 성공적으로 활성화되었습니다."),
    PRODUCT_LIKE_DISABLED(HttpStatus.OK,
            "COMMON200",
            "상품 좋아요가 성공적으로 비활성화되었습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
