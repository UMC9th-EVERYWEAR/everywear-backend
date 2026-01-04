package com.umc.EveryWear.domain.user.controller;

import com.umc.EveryWear.domain.user.dto.UserResponse;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(
            @AuthenticationPrincipal User user
    ) {
        UserResponse userResponse = UserResponse.from(user);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, userResponse);
    }
}
