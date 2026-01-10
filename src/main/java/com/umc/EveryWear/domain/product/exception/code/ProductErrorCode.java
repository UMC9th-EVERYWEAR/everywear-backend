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
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
