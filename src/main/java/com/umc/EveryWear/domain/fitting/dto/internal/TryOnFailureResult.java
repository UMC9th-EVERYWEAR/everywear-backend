package com.umc.EveryWear.domain.fitting.dto.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TryOnFailureResult {

    /**
     * 프론트 모달 분기용 에러 타입
     * INVALID_USER_IMAGE | INVALID_PRODUCT_IMAGE
     */
    private String clientErrorType;

    /**
     * 사용자에게 보여줄 설명
     */
    private String reason;
}
