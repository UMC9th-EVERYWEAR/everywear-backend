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
            "대표사진이 성공적으로 변경되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
