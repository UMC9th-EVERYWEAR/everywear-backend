package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

/**
 * 이 클래스는 FittingStrategy 인터페이스를 구현하며,
 * 기타 카테고리에 대한 피팅 지침을 제공합니다.
 */
@Component
public class OtherFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.OTHER;
    }

    @Override
    public String buildPrompt() {
        return """
                Replace or apply the provided clothing item to the person in a natural and realistic manner,
                allowing the garment to determine its appropriate placement and coverage on the body.
                
                Accurately align the clothing with the relevant body regions it is intended to cover,
                while strictly preserving:
                - the original body pose, posture, and proportions
                - natural anatomy and limb geometry
                - facial features, hair, and skin texture
                - lighting direction, intensity, and color temperature
                
                Ensure realistic garment fit, fabric behavior, folds, and shadows.
                Maintain correct scale, perspective, and seamless integration with existing clothing if layering is required.
                Do not alter the background, camera perspective, or unrelated body parts.
            """;
    }
}