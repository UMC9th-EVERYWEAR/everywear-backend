package com.umc.EveryWear.domain.closet.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ClosetErrorCode implements BaseErrorCode {

    CLOSET_BAD_REQUEST(HttpStatus.BAD_REQUEST,
            "CLOSET400",
            "옷장 조회 요청이 올바르지 않습니다."),
    CLOSET_UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "CLOSET401",
            "옷장 조회를 위해 로그인이 필요합니다."),
    CLOSET_NOT_FOUND(HttpStatus.NOT_FOUND,
            "CLOSET404",
            "옷장 정보를 찾을 수 없습니다."),
    CLOSET_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "CLOSET500",
            "옷장 조회 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
