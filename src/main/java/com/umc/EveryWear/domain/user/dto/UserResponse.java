package com.umc.EveryWear.domain.user.dto;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.enums.SocialType;
import com.umc.EveryWear.domain.user.enums.UserStatus;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private SocialType socialType;
    private UserStatus isActive;
    private Boolean isAgreed;
    private Boolean alarmOnoff;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(Long userId, UserRepository userRepository) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .socialType(user.getSocialType())
                .isActive(user.getIsActive())
                .isAgreed(user.getIsAgreed())
                .alarmOnoff(user.getAlarmOnoff())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}