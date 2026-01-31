package com.umc.EveryWear.domain.product.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements BaseErrorCode {

    CRAWLING_FAILED(HttpStatus.BAD_REQUEST,
            "PRODUCT400",
            "상품 등록에 실패했습니다."),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST,
            "PRODUCT401",
            "지원되지 않는 URL 형식입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "PRODUCT404",
            "상품을 찾을 수 없습니다."),
    PRODUCT_ALREADY_REGISTERED(HttpStatus.CONFLICT,
            "PRODUCT409",
            "이미 등록된 상품입니다."),
    PRODUCT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "PRODUCT410",
            "상품 조회를 위해 로그인이 필요합니다."),
    PRODUCT_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "PRODUCT500",
            "상품 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
