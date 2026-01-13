package com.umc.EveryWear.global.apiPayload.handler;

import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import com.umc.EveryWear.global.apiPayload.code.GeneralErrorCode;
import com.umc.EveryWear.global.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 전역 예외 처리 클래스
 * 애플리케이션 전반에서 발생하는 예외를 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 애플리케이션에서 발생하는 커스텀 예외를 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            GeneralException ex
    ) {

        return ResponseEntity.status(ex.getCode().getStatus())
                .body(ApiResponse.onFailure(
                                ex.getCode(),
                                null
                        )
                );
    }

    // 입력값 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.isEmpty() 
                ? "입력값 검증에 실패했습니다." 
                : fieldErrors.get(0).getDefaultMessage();

        return ResponseEntity.status(GeneralErrorCode.VALID_FAIL.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.VALID_FAIL, errorMessage));
    }

    // 그 외의 정의되지 않은 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, ex.getMessage()));
    }
}
