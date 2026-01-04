package com.umc.EveryWear.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class KakaoUserInfo {
    private String oauthId;
    private String email;
    private String nickname;

    public static KakaoUserInfo from(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return KakaoUserInfo.builder()
                .oauthId(String.valueOf(attributes.get("id")))
                .email((String) kakaoAccount.get("email"))
                .nickname((String) profile.get("nickname"))
                .build();
    }
}