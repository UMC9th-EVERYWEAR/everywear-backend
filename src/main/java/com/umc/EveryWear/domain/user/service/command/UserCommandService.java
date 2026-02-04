package com.umc.EveryWear.domain.user.service.command;

import com.umc.EveryWear.domain.user.dto.res.UserResponseDto;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.exception.code.UserSuccessCode;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;

    /**
     * 약관 동의 토글
     * @param userId 사용자 ID
     * @return 약관 동의 상태 및 메시지
     */
    public UserResponseDto.AgreeToggleResponse toggleAgree(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 약관 동의 토글
        user.toggleAgree();

        // 변경된 상태 저장 (Dirty Checking으로 자동 업데이트되지만 명시적으로 호출)
        userRepository.save(user);

        String message = user.getIsAgreed()
                ? UserSuccessCode.AGREE_ENABLED.getMessage()
                : UserSuccessCode.AGREE_DISABLED.getMessage();

        log.info("사용자 {} 약관 동의 상태 변경: {}", userId, user.getIsAgreed());

        return UserResponseDto.AgreeToggleResponse.builder()
                .isAgreed(user.getIsAgreed())
                .message(message)
                .build();
    }

    /**
     * 알림 설정 토글
     * @param userId 사용자 ID
     * @return 알림 설정 상태 및 메시지
     */
    public UserResponseDto.AlarmToggleResponse toggleAlarm(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 알림 설정 토글
        user.toggleAlarm();

        // 변경된 상태 저장 (Dirty Checking으로 자동 업데이트되지만 명시적으로 호출)
        userRepository.save(user);

        String message = user.getAlarmOn()
                ? UserSuccessCode.ALARM_ENABLED.getMessage()
                : UserSuccessCode.ALARM_DISABLED.getMessage();

        log.info("사용자 {} 알림 설정 변경: {}", userId, user.getAlarmOn());

        return UserResponseDto.AlarmToggleResponse.builder()
                .alarmOn(user.getAlarmOn())
                .message(message)
                .build();
    }
}