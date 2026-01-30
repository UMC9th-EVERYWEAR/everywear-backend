package com.umc.EveryWear.domain.user.controller;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.exception.UserImgException;
import com.umc.EveryWear.domain.user.exception.code.UserImgErrorCode;
import com.umc.EveryWear.domain.user.exception.code.UserImgSuccessCode;
import com.umc.EveryWear.domain.user.service.command.UserImgCommandService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user-images")
@RequiredArgsConstructor
public class UserImgController {

    private final UserImgCommandService userImgCommandService;

    /**
     * 사용자 이미지 검증 및 저장 API
     */
    @Operation(
            summary = "사용자 이미지 검증 by 임준서(개발 완료)",
            description = "사용자가 등록한 사진이 피팅에 적합한 지 검증한 뒤, 적합한 경우에만 프로필사진으로 저장합니다."
    )
    @PostMapping(
            value = "/verify-and-save",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Long> verifyAndSave(
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal User user
    ) {
        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (Exception e) {
            throw new UserImgException(UserImgErrorCode.IMAGE_READ_FAILED);
        }

        Long userImgId =
                userImgCommandService.verifyAndSave(user, imageBytes);

        return ApiResponse.onSuccess(
                UserImgSuccessCode.USER_IMAGE_SAVED,
                userImgId
        );
    }

    /**
     * 대표사진 변경
     */
    @Operation(
            summary = "대표사진 변경 by 임준서(개발 완료)",
            description = "사용자의 프로필사진들 중 하나를 선택하여 대표사진으로 설정합니다."
    )
    @PostMapping("/{userImgId}/representative")
    public ApiResponse<Void> selectRepresentative(
            @PathVariable Long userImgId,
            @AuthenticationPrincipal User user
    ) {
        userImgCommandService.selectRepresentativeImage(user, userImgId);

        return ApiResponse.onSuccess(
                UserImgSuccessCode.REPRESENTATIVE_IMAGE_UPDATED,
                null
        );
    }
}