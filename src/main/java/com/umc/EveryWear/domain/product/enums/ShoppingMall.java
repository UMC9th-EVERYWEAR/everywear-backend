package com.umc.EveryWear.domain.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShoppingMall {
    MUSINSA("무신사", "musinsa"),
    ZIGZAG("지그재그", "zigzag"),
    CM29("29cm", "29cm"),
    WCONCEPT("W컨셉", "wconcept");

    private final String displayName;
    private final String crawlPath;

    public boolean matchesUrl(String url) {
        if (url == null) return false;
        return switch (this) {
            case MUSINSA -> url.matches("^https://www\\.musinsa\\.com/products/\\d+$")
                    || url.matches("^https://musinsa\\.onelink\\.me/[^/]+/[^/]+.*$");
            case ZIGZAG -> url.matches("^https://zigzag\\.kr/catalog/products/\\d+$")
                    || url.matches("^https://s\\.zigzag\\.kr/[A-Za-z0-9]+$");
            case CM29 -> url.matches("^https://www\\.29cm\\.co\\.kr/products/\\d+.*")
                    || url.matches("^https://29cm\\.onelink\\.me/.*$");
            case WCONCEPT -> url.matches("^https://www\\.wconcept\\.co\\.kr/Product/\\d+(\\?.*)?$")
                    || url.matches("^https://m\\.wconcept\\.co\\.kr/Product/\\d+(\\?.*)?$");
        };
    }

    // displayName(한글)으로 enum 조회
    public static ShoppingMall fromDisplayName(String displayName) {
        if (displayName == null) return null;
        for (ShoppingMall m : values()) {
            if (m.getDisplayName().equals(displayName)) return m;
        }
        return null;
    }
}
