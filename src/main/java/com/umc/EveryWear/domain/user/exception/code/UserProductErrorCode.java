package com.umc.EveryWear.domain.user.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserProductErrorCode implements BaseErrorCode {
    USER_PRODUCT_NOTFOUND(
            HttpStatus.NOT_FOUND,
            "USERPRODUCT404_1",
            "유저-상품이 존재하지 않습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
