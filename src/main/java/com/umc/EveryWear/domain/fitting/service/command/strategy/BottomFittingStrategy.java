package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

/**
 * 이 클래스는 FittingStrategy 인터페이스를 구현하며,
 * 하의 카테고리에 대한 피팅 지침을 제공합니다.
 */
@Component
public class BottomFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.BOTTOM;
    }

    @Override
    public String buildPrompt() {
        return """
            Replace the lower-body clothing of the person with the provided garment image.
            Preserve pose, lighting, and lighting consistency.
            """;
    }
}
