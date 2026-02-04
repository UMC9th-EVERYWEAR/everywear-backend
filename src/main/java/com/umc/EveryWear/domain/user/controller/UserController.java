package com.umc.EveryWear.domain.user.controller;

import com.umc.EveryWear.domain.user.dto.res.UserResponseDto;
import com.umc.EveryWear.domain.user.exception.code.UserSuccessCode;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import com.umc.EveryWear.domain.user.service.command.UserCommandService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserCommandService userCommandService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserResponseDto userResponse = UserResponseDto.from(userId, userRepository);
        return ApiResponse.onSuccess(UserSuccessCode.USER_INFO_RETRIEVED, userResponse);
    }

    @Operation(
            summary = "약관 동의 토글",
            description = "사용자의 약관 동의 상태를 토글합니다. 호출할 때마다 true/false가 반전됩니다. default값은 false입니다."
    )
    @PatchMapping("/agree")
    public ApiResponse<UserResponseDto.AgreeToggleResponse> toggleAgree(
            @AuthenticationPrincipal Long userId
    ) {
        UserResponseDto.AgreeToggleResponse response = userCommandService.toggleAgree(userId);

        UserSuccessCode successCode = response.getIsAgreed()
                ? UserSuccessCode.AGREE_ENABLED
                : UserSuccessCode.AGREE_DISABLED;

        return ApiResponse.onSuccess(successCode, response);
    }

    @Operation(
            summary = "알림 설정 토글",
            description = "사용자의 알림 설정 상태를 토글합니다. 호출할 때마다 true/false가 반전됩니다. default값은 ture입니다."
    )
    @PatchMapping("/alarm")
    public ApiResponse<UserResponseDto.AlarmToggleResponse> toggleAlarm(
            @AuthenticationPrincipal Long userId
    ) {
        UserResponseDto.AlarmToggleResponse response = userCommandService.toggleAlarm(userId);

        UserSuccessCode successCode = response.getAlarmOn()
                ? UserSuccessCode.ALARM_ENABLED
                : UserSuccessCode.ALARM_DISABLED;

        return ApiResponse.onSuccess(successCode, response);
    }
}
