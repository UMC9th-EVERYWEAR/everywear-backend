package com.umc.EveryWear.domain.home.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HomeErrorCode implements BaseErrorCode {

    HOME_PRODUCTS_FETCH_FAILED(HttpStatus.BAD_REQUEST,
            "COMMON400",
            "홈 상품 조회에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
