package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

/**
 * 이 클래스는 FittingStrategy 인터페이스를 구현하며,
 * 드레스 카테고리에 대한 피팅 지침을 제공합니다.
 */
@Component
public class DressFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.DRESS;
    }

    @Override
    public String buildPrompt() {
        return """
                Replace the person’s clothing with the provided dress image, covering both the upper and lower body.
                
                Accurately fit the dress to the shoulders, torso, waist, hips, and legs while strictly preserving:
                - the original body pose and posture
                - natural body proportions and anatomy
                - facial features, hair, and skin texture
                - lighting direction, intensity, and color temperature
                
                Ensure smooth garment continuity from top to bottom with realistic fabric flow, folds, and shadows.
                Maintain correct dress length, hemline, and silhouette.
                Do not alter the background, camera perspective, or add/remove accessories.
            """;
    }
}