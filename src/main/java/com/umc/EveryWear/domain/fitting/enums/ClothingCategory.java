package com.umc.EveryWear.domain.fitting.enums;

import com.umc.EveryWear.domain.fitting.exception.FittingException;
import com.umc.EveryWear.domain.fitting.exception.code.FittingErrorCode;

import java.util.Arrays;

/**
 * ClothingCategory 열거형은 의류의 카테고리를 정의합니다.
 * 각 카테고리는 의류의 종류를 나타내며, 문자열로부터 해당 카테고리를 매핑하는 기능을 제공합니다.
 */
public enum ClothingCategory {
    TOP,
    BOTTOM,
    OUTER,
    DRESS,
    OTHER;

    /**
     * 주어진 문자열을 해당하는 ClothingCategory 열거형으로 변환합니다.
     * 지원되지 않는 문자열이 입력될 경우 IllegalArgumentException을 발생시킵니다.
     * @param raw 변환할 문자열
     * @return 해당하는 ClothingCategory 열거형
     * @throws IllegalArgumentException 지원되지 않는 문자열이 입력된 경우
     * */
    public static ClothingCategory from(String raw) {
        if (raw == null || raw.isBlank()) {
            // 옷 카테고리가 null이거나 빈 문자열인 경우 예외 처리
            throw new FittingException(FittingErrorCode.INVALID_PRODUCT_CATEGORY);
        }

        return switch (raw.toLowerCase()) {
            case "top", "상의", "tshirt", "shirt" -> TOP;
            case "bottom", "하의", "pants", "jeans" -> BOTTOM;
            case "outer", "아우터", "jacket", "coat" -> OUTER;
            case "dress", "원피스" -> DRESS;
            // 그 외의 모든 경우는 OTHER로 매핑
            default -> OTHER;
        };
    }
}