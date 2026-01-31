package com.umc.EveryWear.domain.product.exception.code;

import com.umc.EveryWear.domain.product.enums.ShoppingMall;
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
    MUSINSA_PRODUCT_UPDATED(HttpStatus.OK,
            "200",
            "무신사 상품을 업데이트했습니다."),
    MUSINSA_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "200",
            "무신사 상품의 URL을 업데이트했습니다."),
    ZIGZAG_PRODUCT_ADDED(HttpStatus.OK,
            "200",
            "지그재그 상품을 추가했습니다."),
    ZIGZAG_PRODUCT_UPDATED(HttpStatus.OK,
            "200",
            "지그재그 상품을 업데이트했습니다."),
    ZIGZAG_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "200",
            "지그재그 상품의 URL을 업데이트했습니다."),
    WCONCEPT_PRODUCT_ADDED(HttpStatus.OK,
            "200",
            "W컨셉 상품을 추가했습니다."),
    WCONCEPT_PRODUCT_UPDATED(HttpStatus.OK,
            "200",
            "W컨셉 상품을 업데이트했습니다."),
    WCONCEPT_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "200",
            "W컨셉 상품의 URL을 업데이트했습니다."),
    CM29_PRODUCT_ADDED(HttpStatus.OK,
            "200",
            "29cm 상품을 추가했습니다."),
    CM29_PRODUCT_UPDATED(HttpStatus.OK,
            "200",
            "29cm 상품을 업데이트했습니다."),
    CM29_PRODUCT_URL_UPDATED(HttpStatus.OK,
            "200",
            "29cm 상품의 URL을 업데이트했습니다."),
    PRODUCTS_RETRIEVED(HttpStatus.OK,
            "200",
            "전체 상품을 조회했습니다."),
    TOP_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "200",
            "상의 상품을 조회했습니다."),
    BOTTOM_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "200",
            "하의 상품을 조회했습니다."),
    OUTER_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "200",
            "아우터 상품을 조회했습니다."),
    DRESS_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "200",
            "원피스 상품을 조회했습니다."),
    ETC_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "200",
            "기타 상품을 조회했습니다."),
    PRODUCT_LIKE_ENABLED(HttpStatus.OK,
            "200",
            "상품 좋아요가 성공적으로 활성화되었습니다."),
    PRODUCT_LIKE_DISABLED(HttpStatus.OK,
            "200",
            "상품 좋아요가 성공적으로 비활성화되었습니다."),
    ;

    // 좋아요 토글 API 응답용
    public static ProductSuccessCode forLikeToggle(boolean isLiked) {
        return Boolean.TRUE.equals(isLiked) ? PRODUCT_LIKE_ENABLED : PRODUCT_LIKE_DISABLED;
    }

    // 상품 등록 API 응답용: 쇼핑몰·업데이트 여부에 따른 성공 코드
    public static ProductSuccessCode forImportResult(ShoppingMall mall, boolean isUrlUpdated, boolean isUpdated) {
        if (isUrlUpdated) {
            return switch (mall) {
                case MUSINSA -> MUSINSA_PRODUCT_URL_UPDATED;
                case ZIGZAG -> ZIGZAG_PRODUCT_URL_UPDATED;
                case CM29 -> CM29_PRODUCT_URL_UPDATED;
                case WCONCEPT -> WCONCEPT_PRODUCT_URL_UPDATED;
            };
        }
        if (isUpdated) {
            return switch (mall) {
                case MUSINSA -> MUSINSA_PRODUCT_UPDATED;
                case ZIGZAG -> ZIGZAG_PRODUCT_UPDATED;
                case CM29 -> CM29_PRODUCT_UPDATED;
                case WCONCEPT -> WCONCEPT_PRODUCT_UPDATED;
            };
        }
        return switch (mall) {
            case MUSINSA -> MUSINSA_PRODUCT_ADDED;
            case ZIGZAG -> ZIGZAG_PRODUCT_ADDED;
            case CM29 -> CM29_PRODUCT_ADDED;
            case WCONCEPT -> WCONCEPT_PRODUCT_ADDED;
        };
    }

    private final HttpStatus status;
    private final String code;
    private final String message;
}
