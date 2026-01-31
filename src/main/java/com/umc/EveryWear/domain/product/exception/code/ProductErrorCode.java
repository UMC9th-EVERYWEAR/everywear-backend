package com.umc.EveryWear.domain.product.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements BaseErrorCode {

    CRAWLING_FAILED(HttpStatus.BAD_REQUEST,
            "400",
            "상품 등록에 실패했습니다."),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST,
            "400",
            "지원되지 않는 url 형식입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "404",
            "수정하려는 상품을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
