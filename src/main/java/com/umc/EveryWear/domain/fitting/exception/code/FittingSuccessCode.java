package com.umc.EveryWear.domain.fitting.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FittingSuccessCode implements BaseSuccessCode {


    /**
     * 사용자 이미지 검증
     */
    IMAGE_VERIFICATION_SUCCESS(
            HttpStatus.OK,
            "FITTING200_1",
            "사용자 이미지 검증에 성공했습니다."
    ),

    /**
     * 가상 피팅 이미지 생성
     */
    FITTING_IMAGE_GENERATED(
            HttpStatus.OK,
            "FITTING200_2",
            "가상 피팅 이미지 생성에 성공했습니다."
    ),

    /**
     * 피팅 프로세스 완료
     */
    FITTING_PROCESS_COMPLETED(
            HttpStatus.OK,
            "FITTING200_3",
            "피팅이 성공적으로 완료되었습니다."
    ),

    /**
     * 내 피팅 목록 조회 성공
     */
    FITTING_LIST_FETCHED(
            HttpStatus.OK,
            "FITTING200_4",
            "내 피팅 목록 조회에 성공했습니다."
    ),

    /**
     * 피팅 상세 조회 성공
     */
    FITTING_DETAIL_FETCHED(
            HttpStatus.OK,
            "FITTING200_5",
            "피팅 상세 조회에 성공했습니다."
    ),

    /**
     * 피팅 좋아요 성공
     */
    FITTING_LIKE_TOGGLED(
            HttpStatus.OK,
            "FITTING200_6",
            "피팅 좋아요 상태 변경에 성공했습니다."
    ),

    /**
     * 좋아요한 피팅 목록 조회 성공
     */
    LIKED_FITTING_LIST_FETCHED(
            HttpStatus.OK,
            "FITTING200_7",
            "좋아요한 피팅 목록 조회에 성공했습니다."
    ),

    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
