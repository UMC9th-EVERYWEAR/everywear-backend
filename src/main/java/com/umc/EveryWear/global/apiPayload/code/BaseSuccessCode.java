package com.umc.EveryWear.global.apiPayload.code;

import org.springframework.http.HttpStatus;

/**
 * 모든 SuccessCode Enum이 implements 하는 인터페이스
 */
public interface BaseSuccessCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
