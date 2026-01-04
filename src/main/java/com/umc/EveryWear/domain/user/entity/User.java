package com.umc.EveryWear.domain.user.entity;

import com.umc.EveryWear.domain.user.enums.UserStatus;
import com.umc.EveryWear.domain.user.enums.SocialType;
import com.umc.EveryWear.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private UserStatus isActive;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "is_agreed", nullable = false)
    private Boolean isAgreed;

    @Column(name = "alarm_onoff", nullable = false)
    private Boolean alarmOnoff;

    @Builder
    public User(Long userId, String oauthId, String name, String password, String email,
                SocialType socialType, UserStatus isActive, String refreshToken,
                Boolean isAgreed, Boolean alarmOnoff) {
        this.userId = userId;
        this.oauthId = oauthId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.socialType = socialType;
        this.isActive = isActive;
        this.refreshToken = refreshToken;
        this.isAgreed = isAgreed;
        this.alarmOnoff = alarmOnoff;
    }

    // Refresh Token 업데이트 메서드
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
