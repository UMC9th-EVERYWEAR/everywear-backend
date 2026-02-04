package com.umc.EveryWear.domain.user.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserSuccessCode implements BaseSuccessCode {

    USER_INFO_RETRIEVED(
            HttpStatus.OK,
            "USER200_0",
            "사용자 정보를 조회했습니다."
    ),
    AGREE_ENABLED(
            HttpStatus.OK,
            "USER200_1",
            "약관에 동의하였습니다."
    ),
    AGREE_DISABLED(
            HttpStatus.OK,
            "USER200_2",
            "약관 동의를 철회하였습니다."
    ),
    ALARM_ENABLED(
            HttpStatus.OK,
            "USER200_3",
            "알림을 켰습니다."
    ),
    ALARM_DISABLED(
            HttpStatus.OK,
            "USER200_4",
            "알림을 껐습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}