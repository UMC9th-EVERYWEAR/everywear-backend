package com.umc.EveryWear.domain.fitting.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FittingErrorCode implements BaseErrorCode {


    INVALID_USER_IMAGE(
            HttpStatus.BAD_REQUEST,
            "FITTING400_1",
            "피팅에 적합하지 않은 사용자 이미지입니다."
    ),

    MULTIPLE_PEOPLE_DETECTED(
            HttpStatus.BAD_REQUEST,
            "FITTING400_2",
            "사진에 여러 사람이 감지되었습니다. 한 명만 나온 사진을 사용해주세요."
    ),

    BODY_CROPPED(
            HttpStatus.BAD_REQUEST,
            "FITTING400_3",
            "신체 일부가 잘려 있습니다. 전신이 보이도록 촬영해주세요."
    ),

    INVALID_POSE(
            HttpStatus.BAD_REQUEST,
            "FITTING400_4",
            "자연스러운 정면 자세의 사진을 사용해주세요."
    ),

    POOR_LIGHTING(
            HttpStatus.BAD_REQUEST,
            "FITTING400_5",
            "조명이 어두워 인식이 어렵습니다. 밝은 환경에서 촬영해주세요."
    ),

    OBSTRUCTION_DETECTED(
            HttpStatus.BAD_REQUEST,
            "FITTING400_6",
            "의류 영역을 가리는 물체가 있습니다."
    ),

    INVALID_PRODUCT_CATEGORY(
            HttpStatus.BAD_REQUEST,
            "FITTING400_8",
            "지원하지 않는 의류 카테고리입니다."
    ),

    IMAGE_READ_FAILED(
            HttpStatus.BAD_REQUEST,
            "FITTING400_9",
            "이미지 파일을 읽는 데 실패했습니다."
    ),

    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FITTING404_1",
            "해당 상품을 찾을 수 없습니다."
    ),

    FITTING_HISTORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FITTING404_2",
            "해당 피팅 기록을 찾을 수 없습니다."
    ),

    AI_RESPONSE_EMPTY(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FITTING500_1",
            "AI 응답을 받지 못했습니다."
    ),

    AI_VERIFICATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FITTING500_2",
            "이미지 검증 중 오류가 발생했습니다."
    ),

    AI_GENERATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FITTING500_3",
            "가상 피팅 이미지 생성에 실패했습니다."
    ),
    AI_RESPONSE_INVALID_FORMAT(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FITTING500_10",
            "AI 응답 형식이 올바르지 않습니다."
    )
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
