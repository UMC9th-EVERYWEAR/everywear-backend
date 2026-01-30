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
}
