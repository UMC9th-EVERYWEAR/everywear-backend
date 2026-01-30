package com.umc.EveryWear.domain.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShoppingMall {
    MUSINSA("무신사"),
    ZIGZAG("지그재그"),
    CM29("29cm"),
    WCONCEPT("W컨셉");

    private final String displayName;
}
