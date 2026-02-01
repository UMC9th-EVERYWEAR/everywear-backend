package com.umc.EveryWear.domain.user.exception.code;

import com.umc.EveryWear.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserImgErrorCode implements BaseErrorCode {

    IMAGE_READ_FAILED(
            HttpStatus.BAD_REQUEST,
            "USERIMG400_1",
            "이미지 파일을 읽는 데 실패했습니다."),
    INVALID_USER_IMAGE(
            HttpStatus.BAD_REQUEST,
            "USERIMG400_2",
            "피팅에 적합하지 않은 이미지입니다."),
    IMAGE_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "USERIMG400_3",
            "저장 가능한 이미지 개수를 초과했습니다."),
    USER_IMAGE_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "USERIMG400_4",
            "저장 가능 최대 대표사진 수는 5장 입니다."),
    USER_REPRESENTATIVE_IMG_CANNOT_DELETE(
            HttpStatus.BAD_REQUEST,
            "USERIMG_400_5",
            "대표 사진은 삭제할 수 없습니다."),

    USER_IMAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USERIMG404_1",
            "사용자 이미지를 찾을 수 없습니다."),

    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "USERIMG500_1",
            "사용자 이미지 처리 중 서버 오류가 발생했습니다."),


    REPRESENTATIVE_IMAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USERIMG404_2",
            "사용자 대표 사진을 찾을 수 없습니다."),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USERIMG404_3",
            "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
