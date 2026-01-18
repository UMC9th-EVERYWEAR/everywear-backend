package com.umc.EveryWear.domain.user.controller;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.exception.UserImgException;
import com.umc.EveryWear.domain.user.exception.code.UserImgErrorCode;
import com.umc.EveryWear.domain.user.exception.code.UserImgSuccessCode;
import com.umc.EveryWear.domain.user.service.command.UserImgCommandService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user-images")
@RequiredArgsConstructor
public class UserImgController {

    private final UserImgCommandService userImgCommandService;

    /**
     * 사용자 이미지 검증 및 저장 API
     */
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
}