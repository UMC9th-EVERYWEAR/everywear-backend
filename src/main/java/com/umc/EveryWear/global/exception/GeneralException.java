package com.umc.EveryWear.global.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
/**
 * 공통 예외 처리 클래스
 * 모든 커스텀 예외는 이 클래스를 상속받아 구현
 */
public class GeneralException extends RuntimeException {
    private final BaseErrorCode code;
}
