package com.umc.EveryWear.domain.user.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserImgSuccessCode implements BaseSuccessCode {

    USER_IMAGE_SAVED(
            HttpStatus.CREATED,
            "USERIMG201_1",
            "사용자 이미지가 성공적으로 저장되었습니다."
    ),
    REPRESENTATIVE_IMAGE_UPDATED(
            HttpStatus.OK,
            "USERIMG200_1",
            "대표사진이 성공적으로 변경되었습니다."),

    REPRESENTIVE_IMG_200(
            HttpStatus.OK,
            "REPRESENTIVE_IMG_200",
            "사용자의 대표사진 조회에 성공합니다."),

    USER_IMG_DELETED(
            HttpStatus.OK,
            "USERIMG200_2",
            "사용자 이미지가 성공적으로 삭제되었습니다."),

    USER_IMG_LIST_FETCHED(
            HttpStatus.OK,
            "USERIMG200_3",
            "사용자 이미지 목록이 성공적으로 조회되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
