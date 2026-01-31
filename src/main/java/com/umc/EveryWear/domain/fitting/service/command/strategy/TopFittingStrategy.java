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
                Replace only the upper-body clothing of the person with the provided garment image.
                
                Accurately fit the garment to the person’s torso, shoulders, and arms while strictly preserving:
                - the original body pose and posture
                - body proportions and anatomy
                - facial features, hair, and skin texture
                - lighting direction, intensity, and color temperature
                
                Ensure natural fabric alignment, realistic folds, and correct garment boundaries.
                Do not alter the background, lower-body clothing, or overall image composition.
            """;
    }
}
