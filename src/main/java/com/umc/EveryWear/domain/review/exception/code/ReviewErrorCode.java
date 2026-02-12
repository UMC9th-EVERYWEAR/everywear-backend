package com.umc.EveryWear.domain.review.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "REVIEW404_1",
            "상품을 찾을 수 없습니다"),
    CRAWLING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "REVIEW500_1",
            "리뷰 크롤링에 실패했습니다"),
    REVIEW_COUNT_INSUFFICIENT(HttpStatus.BAD_REQUEST,
            "REVIEW400_1",
            "리뷰 수가 부족하여 AI 리뷰 요약이 불가능합니다."),
    ;


    private final HttpStatus status;
    private final String code;
    private final String message;
}