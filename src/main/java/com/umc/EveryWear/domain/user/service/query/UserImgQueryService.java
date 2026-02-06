package com.umc.EveryWear.domain.user.service.query;

import com.umc.EveryWear.domain.user.dto.res.UserImgResponseDto;
import com.umc.EveryWear.domain.user.entity.UserImg;
import com.umc.EveryWear.domain.user.exception.UserImgException;
import com.umc.EveryWear.domain.user.exception.code.UserImgErrorCode;
import com.umc.EveryWear.domain.user.repository.UserImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserImgQueryService {

    private final UserImgRepository userImgRepository;

    /**
     * 유저 프로필 이미지 목록 조회
     */
    public List<UserImgResponseDto.UserImgQuery> getProfileImages(Long userId) {
        return userImgRepository.findAllByUser_UserId(userId).stream()
                .map(img -> new UserImgResponseDto.UserImgQuery(
                        img.getProfileImageId(),
                        img.getImageUrl(),
                        img.isRepresentative()
                ))
                .toList();
    }

    /**
     * 사용자 대표사진 조회
     */
    public UserImg getRepresentativeImage(Long userId) {
        return userImgRepository
                .findByUser_UserIdAndRepresentativeTrue(userId)
                .orElseThrow(() ->
                        new UserImgException(UserImgErrorCode.REPRESENTATIVE_IMAGE_NOT_FOUND)
                );
    }
}
