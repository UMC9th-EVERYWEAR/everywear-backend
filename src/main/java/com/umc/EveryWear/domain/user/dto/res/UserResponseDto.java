package com.umc.EveryWear.domain.user.dto.res;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.enums.SocialType;
import com.umc.EveryWear.domain.user.enums.UserStatus;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDto {
    private Long userId;
    private String name;
    private String email;
    private SocialType socialType;
    private UserStatus isActive;
    private Boolean isAgreed;
    private Boolean alarmOn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponseDto from(Long userId, UserRepository userRepository) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return UserResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .socialType(user.getSocialType())
                .isActive(user.getIsActive())
                .isAgreed(user.getIsAgreed())
                .alarmOn(user.getAlarmOn())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AgreeToggleResponse {
        private Boolean isAgreed;
        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AlarmToggleResponse {
        private Boolean alarmOn;
        private String message;
    }
}