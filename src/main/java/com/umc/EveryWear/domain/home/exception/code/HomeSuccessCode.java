package com.umc.EveryWear.domain.home.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HomeSuccessCode implements BaseSuccessCode {

    HOME_PRODUCTS_RETRIEVED(HttpStatus.OK,
            "HOME200",
            "홈 화면 상품을 조회했습니다."),
    RECENT_FITTINGS_FETCHED(HttpStatus.OK,
            "HOME201",
            "홈 화면 최근 피팅내역을 조회했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
