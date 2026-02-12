package com.umc.EveryWear.global.exception;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
/**
 * 공통 예외 처리 클래스
 * 모든 커스텀 예외는 이 클래스를 상속받아 구현
 */
public class GeneralException extends RuntimeException {
    private final BaseErrorCode code;

    // 기본 생성자
    public GeneralException(BaseErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

    // ⭐ reason 전달용 생성자
    public GeneralException(BaseErrorCode code, String reason) {
        super(reason);
        this.code = code;
    }
}
