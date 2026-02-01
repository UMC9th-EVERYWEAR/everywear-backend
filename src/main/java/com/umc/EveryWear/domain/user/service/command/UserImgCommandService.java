package com.umc.EveryWear.domain.user.service.command;

import com.umc.EveryWear.domain.fitting.client.GeminiImageClient;
import com.umc.EveryWear.domain.fitting.dto.internal.VerificationResult;
import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.UserImg;
import com.umc.EveryWear.domain.user.exception.UserImgException;
import com.umc.EveryWear.domain.user.exception.code.UserImgErrorCode;
import com.umc.EveryWear.domain.user.repository.UserImgRepository;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import com.umc.EveryWear.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserImgCommandService {

    private static final int MAX_IMAGE_COUNT = 5;

    private final GeminiImageClient geminiImageClient;
    private final UserImgRepository userImgRepository;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;

    /**
     * 사용자 이미지 검증 후 저장
     *
     * @param userId       로그인 사용자
     * @param imageBytes 업로드된 이미지 바이트
     * @return 생성된 UserImg ID
     */
    public Long verifyAndSave(
            Long userId,
            byte[] imageBytes
    ) {
        // 사용자 존재 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserImgException(UserImgErrorCode.USER_NOT_FOUND)
                );

        // 1. 이미지 개수 제한
        long imageCount =
                userImgRepository.countByUser_UserId(userId);

        if (imageCount >= MAX_IMAGE_COUNT) {
            throw new UserImgException(UserImgErrorCode.USER_IMAGE_LIMIT_EXCEEDED);
        }

        // 2. AI 이미지 검증
        VerificationResult verification =
                geminiImageClient.verifyUserImage(imageBytes);

        if (!verification.isSuitable()) {
            throw new UserImgException(
                    UserImgErrorCode.INVALID_USER_IMAGE,
                    verification.reason());
        }

        // 3. S3 업로드
        String imageUrl = s3Uploader.upload(
                imageBytes,
                "user-profile"
        );

        // 4. 대표사진 여부 결정
        boolean hasRepresentative =
                userImgRepository
                        .findByUser_UserIdAndRepresentativeTrue(userId)
                        .isPresent();

        UserImg userImg = UserImg.builder()
                .user(user)
                .imageUrl(imageUrl)
                .representative(!hasRepresentative)
                .build();

        userImgRepository.save(userImg);

        return userImg.getProfileImageId();
    }

    /**
     * 대표 이미지 선택
     */
    public void selectRepresentativeImage(
            Long userId,
            Long userImgId
    ) {
        // 1. 선택한 이미지 검증
        UserImg targetImg = userImgRepository.findById(userImgId)
                .filter(img -> img.getUser().getUserId().equals(userId))
                .orElseThrow(() ->
                        new UserImgException(UserImgErrorCode.USER_IMAGE_NOT_FOUND)
                );

        // 2. 기존 대표 이미지 해제
        userImgRepository.findByUser_UserIdAndRepresentativeTrue(userId)
                .ifPresent(UserImg::cancelRepresentative);

        // 3. 새 대표 이미지 지정
        targetImg.makeRepresentative();
    }
}