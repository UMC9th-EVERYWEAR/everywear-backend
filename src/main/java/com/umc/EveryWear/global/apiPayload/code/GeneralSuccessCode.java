package com.umc.EveryWear.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 성공 코드
 */
@AllArgsConstructor
@Getter
public enum GeneralSuccessCode implements BaseSuccessCode {

    OK(HttpStatus.OK,
            "COMMON200",
            "성공적으로 요청을 처리했습니다."),
    CREATED(HttpStatus.CREATED,
            "COMMON201_1",
            "리소스가 성공적으로 생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED,
            "COMMON202_1",
            "요청이 수락되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT,
            "COMMON204_1",
            "요청이 성공적으로 처리되었습니다. 응답 본문이 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}