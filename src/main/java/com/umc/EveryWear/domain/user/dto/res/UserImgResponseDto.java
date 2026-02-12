package com.umc.EveryWear.domain.user.dto.res;

import com.umc.EveryWear.domain.user.entity.UserImg;

public class UserImgResponseDto {

    public record UserImgQuery(
            Long profileImageId,
            String imageUrl,
            boolean representative
    ) {
        public static UserImgQuery from(UserImg img) {
            return new UserImgQuery(
                    img.getProfileImageId(),
                    img.getImageUrl(),
                    img.isRepresentative()
            );
        }
    }

    // 대표 이미지 단건 조회 응답
    public record RepresentativeImgResponse(UserImgQuery representative_img) {
        
    }
}
