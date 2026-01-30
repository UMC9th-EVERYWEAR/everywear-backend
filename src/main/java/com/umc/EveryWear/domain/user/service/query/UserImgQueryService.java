package com.umc.EveryWear.domain.user.service.query;

import com.umc.EveryWear.domain.user.dto.res.UserImgResponseDto;
import com.umc.EveryWear.domain.user.entity.User;
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
     * 대표 이미지 조회
     */
    public UserImgResponseDto.UserImgQuery getRepresentative(User user) {
        UserImg img = userImgRepository
                .findByUserAndRepresentativeTrue(user)
                .orElseThrow(() ->
                        new UserImgException(UserImgErrorCode.REPRESENTATIVE_IMAGE_NOT_FOUND));

        return UserImgResponseDto.UserImgQuery.from(img);
    }

    /**
     * 전체 프로필 이미지 조회
     */
    public List<UserImgResponseDto.UserImgQuery> getAll(User user) {
        return userImgRepository.findAllByUser(user)
                .stream()
                .map(UserImgResponseDto.UserImgQuery::from)
                .toList();
    }
}
