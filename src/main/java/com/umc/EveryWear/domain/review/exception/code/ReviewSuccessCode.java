package com.umc.EveryWear.domain.review.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewSuccessCode implements BaseSuccessCode {

    REVIEW_FOUND(HttpStatus.OK,
            "REVIEW200_1",
            "리뷰 조회 성공"),
    REVIEW_CRAWLING(HttpStatus.OK,
            "REVIEW200_2",
            "리뷰 크롤링 진행 중"),
    REVIEW_FAILED(HttpStatus.OK,
            "REVIEW200_3",
            "리뷰 크롤링 실패"),
    REVIEW_NOT_STARTED(HttpStatus.OK,
            "REVIEW200_4",
            "리뷰 크롤링이 필요합니다"),
    REVIEW_CRAWL_STARTED(HttpStatus.ACCEPTED,
            "REVIEW202_1",
            "리뷰 크롤링이 시작되었습니다"),
    REVIEW_ALREADY_CRAWLING(HttpStatus.ACCEPTED,
            "REVIEW202_2",
            "리뷰 크롤링이 진행 중입니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}