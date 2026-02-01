package com.umc.EveryWear.domain.product.enums;

import com.umc.EveryWear.domain.product.exception.code.ProductSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 상품 카테고리
@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    TOP("상의", ProductSuccessCode.TOP_PRODUCTS_RETRIEVED),
    BOTTOM("하의", ProductSuccessCode.BOTTOM_PRODUCTS_RETRIEVED),
    OUTER("아우터", ProductSuccessCode.OUTER_PRODUCTS_RETRIEVED),
    DRESS("원피스", ProductSuccessCode.DRESS_PRODUCTS_RETRIEVED),
    ETC("기타", ProductSuccessCode.ETC_PRODUCTS_RETRIEVED);

    private final String value;
    private final ProductSuccessCode successCode;
}
