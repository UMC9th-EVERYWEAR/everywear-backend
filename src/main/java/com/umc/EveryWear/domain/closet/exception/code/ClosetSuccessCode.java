package com.umc.EveryWear.domain.closet.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ClosetSuccessCode implements BaseSuccessCode {

    CLOSET_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "CLOSET200",
            "내 옷장 전체 상품을 조회했습니다."),
    CLOSET_TOP_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "CLOSET201",
            "내 옷장 상의 상품을 조회했습니다."),
    CLOSET_BOTTOM_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "CLOSET202",
            "내 옷장 하의 상품을 조회했습니다."),
    CLOSET_OUTER_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "CLOSET203",
            "내 옷장의 아우터 상품을 조회했습니다."),
    CLOSET_DRESS_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "CLOSET204",
            "내 옷장 원피스 상품을 조회했습니다."),
    CLOSET_ETC_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "CLOSET205",
            "내 옷장의 기타 상품을 조회했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
