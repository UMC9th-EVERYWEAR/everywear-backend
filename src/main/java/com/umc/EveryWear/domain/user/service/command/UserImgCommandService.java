package com.umc.EveryWear.domain.user.service.command;

import com.umc.EveryWear.domain.fitting.client.GeminiImageClient;
import com.umc.EveryWear.domain.fitting.dto.internal.VerificationResult;
import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.UserImg;
import com.umc.EveryWear.domain.user.exception.UserImgException;
import com.umc.EveryWear.domain.user.exception.code.UserImgErrorCode;
import com.umc.EveryWear.domain.user.repository.UserImgRepository;
import com.umc.EveryWear.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserImgCommandService {

    private final GeminiImageClient geminiImageClient;
    private final UserImgRepository userImgRepository;
    private final S3Uploader s3Uploader;

    /**
     * 사용자 이미지 검증 후 저장
     *
     * @param user       로그인 사용자
     * @param imageBytes 업로드된 이미지 바이트
     * @return 생성된 UserImg ID
     */
    public Long verifyAndSave(
            User user,
            byte[] imageBytes
    ) {
        // AI 이미지 검증
        VerificationResult verification =
                geminiImageClient.verifyUserImage(imageBytes);

        if (!verification.isSuitable()) {
            throw new UserImgException(
                    UserImgErrorCode.INVALID_USER_IMAGE,
                    verification.reason());
        }

        // 2. S3 업로드
        String imageUrl = s3Uploader.upload(
                imageBytes,
                "user-profile"
        );

        // 3. UserImg 저장
        UserImg userImg = userImgRepository.save(
                UserImg.builder()
                        .user(user)
                        .imageUrl(imageUrl)
                        .build()
        );

        return userImg.getProfileImageId();
    }
}