package com.umc.EveryWear.domain.user.service;

import com.umc.EveryWear.domain.user.dto.KakaoUserInfo;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.enums.SocialType;
import com.umc.EveryWear.domain.user.enums.UserStatus;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("OAuth2 Login - Provider: {}", registrationId);
        log.info("OAuth2 User Attributes: {}", attributes);

        // 카카오 로그인 처리
        if ("kakao".equals(registrationId)) {
            KakaoUserInfo kakaoUserInfo = KakaoUserInfo.from(attributes);
            User user = saveOrUpdate(kakaoUserInfo);

            return new CustomOAuth2User(user, attributes);
        }

        throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    }

    private User saveOrUpdate(KakaoUserInfo kakaoUserInfo) {
        User user = userRepository.findByOauthIdAndSocialType(
                kakaoUserInfo.getOauthId(),
                SocialType.KAKAO
        ).orElse(null);

        if (user == null) {
            // 신규 사용자 등록
            user = User.builder()
                    .oauthId(kakaoUserInfo.getOauthId())
                    .name(kakaoUserInfo.getNickname())
                    .email(kakaoUserInfo.getEmail())
                    .password("") // OAuth 사용자는 비밀번호 불필요
                    .socialType(SocialType.KAKAO)
                    .isActive(UserStatus.ACTIVE)
                    .refreshToken("")
                    .isAgreed(false)
                    .alarmOnoff(true)
                    .build();

            return userRepository.save(user);
        }

        return user;
    }
}
