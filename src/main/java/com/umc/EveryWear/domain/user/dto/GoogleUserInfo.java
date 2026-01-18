package com.umc.EveryWear.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class GoogleUserInfo {
    private String oauthId;
    private String email;
    private String name;
    private String picture;

    public static GoogleUserInfo from(Map<String, Object> attributes) {
        return GoogleUserInfo.builder()
                .oauthId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .build();
    }
}