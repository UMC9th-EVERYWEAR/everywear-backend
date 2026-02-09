package com.umc.EveryWear.domain.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCrawlStatus {
    PROCESSING("크롤링 진행 중"),
    COMPLETED("크롤링 완료"),
    FAILED("크롤링 실패");

    private final String description;
}
