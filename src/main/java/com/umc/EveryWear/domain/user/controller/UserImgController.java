package com.umc.EveryWear.domain.user.controller;

import com.umc.EveryWear.domain.user.dto.res.UserImgResponseDto;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.exception.UserImgException;
import com.umc.EveryWear.domain.user.exception.code.UserImgErrorCode;
import com.umc.EveryWear.domain.user.exception.code.UserImgSuccessCode;
import com.umc.EveryWear.domain.user.service.command.UserImgCommandService;
import com.umc.EveryWear.domain.user.service.query.UserImgQueryService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user-images")
@RequiredArgsConstructor
public class UserImgController {

    private final UserImgCommandService userImgCommandService;
    private final UserImgQueryService userImgQueryService;

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
            @AuthenticationPrincipal Long userId
    ) {
        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (Exception e) {
            throw new UserImgException(UserImgErrorCode.IMAGE_READ_FAILED);
        }

        Long userImgId =
                userImgCommandService.verifyAndSave(userId, imageBytes);

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
            @AuthenticationPrincipal Long userId
    ) {
        userImgCommandService.selectRepresentativeImage(userId, userImgId);

        return ApiResponse.onSuccess(
                UserImgSuccessCode.REPRESENTATIVE_IMAGE_UPDATED,
                null
        );
    }

    // 대표 이미지 조회
    @Operation(
            summary = "대표 이미지 조회",
            description = "사용자의 대표사진을 조회합니다."
    )
    @GetMapping("/representative")
    public ApiResponse<UserImgResponseDto.RepresentativeImgResponse> getRepresentativeImage(
            @AuthenticationPrincipal Long userId
    ) {
        var img = userImgQueryService.getRepresentativeImage(userId);
        UserImgResponseDto.RepresentativeImgResponse result =
                new UserImgResponseDto.RepresentativeImgResponse(UserImgResponseDto.UserImgQuery.from(img));
        return ApiResponse.onSuccess(UserImgSuccessCode.REPRESENTIVE_IMG_200, result);
    }

    // 프로필 이미지 목록 조회
    @Operation(
            summary = "프로필 사진 조회 by 임준서(개발 완료)",
            description = "사용자의 모든 프로필사진들을 조회합니다."
    )
    @GetMapping
    public List<UserImgResponseDto.UserImgQuery> getProfileImages(
            @AuthenticationPrincipal Long userId
    ) {
        return userImgQueryService.getProfileImages(userId);
    }

    /**
     * 프로필 이미지 삭제
     */
    @Operation(
            summary = "프로필 사진 삭제 by 임준서(개발 완료)",
            description = "사용자가 선택한 프로필사진을 삭제합니다."
    )
    @DeleteMapping("/{imageId}")
    public void deleteProfileImage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long imageId
    ) {
        userImgCommandService.deleteProfileImage(userId, imageId);
    }
}