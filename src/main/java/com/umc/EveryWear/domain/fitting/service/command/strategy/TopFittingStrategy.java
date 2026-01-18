package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

/**
 * 이 클래스는 FittingStrategy 인터페이스를 구현하며,
 * 상의 카테고리에 대해 적합한 피팅 지침을 제공합니다.
 */
@Component
public class TopFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.TOP;
    }

    @Override
    public String buildPrompt() {
        return """
            Replace the upper-body clothing of the person with the provided garment image.
            Preserve pose, lighting, and body proportions.
            """;
    }
}
