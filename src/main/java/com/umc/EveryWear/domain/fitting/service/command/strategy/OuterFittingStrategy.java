package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

/**
 * 이 클래스는 FittingStrategy 인터페이스를 구현하며,
 * 아우터 카테고리에 대한 피팅 지침을 제공합니다.
 */
@Component
public class OuterFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.OUTER;
    }

    @Override
    public String buildPrompt() {
        return """
                Overlay the provided outerwear garment onto the person while keeping the inner clothing unchanged.
                
                Naturally align the outerwear with the shoulders, arms, and torso while strictly preserving:
                - the original body pose and posture
                - body proportions and limb geometry
                - facial features, hair, and inner clothing details
                - lighting direction, intensity, and consistency
                
                Ensure realistic layering, proper garment thickness, and correct sleeve positioning.
                Maintain natural fabric folds, shadows, and garment edges.
                Do not modify the background, camera angle, or inner garments.
            """;
    }
}