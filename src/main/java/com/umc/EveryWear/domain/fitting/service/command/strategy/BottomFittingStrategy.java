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
                Replace only the lower-body clothing of the person with the provided garment image.
                
                    Precisely align the garment to the hips, waist, and legs while strictly preserving:
                    - the original body pose and stance
                    - body proportions and leg geometry
                    - upper-body clothing, face, and hairstyle
                    - lighting direction, intensity, and overall lighting consistency
                
                    Maintain realistic fabric draping, shadows, and garment edges.
                    Do not modify the background, upper-body clothing, or camera perspective.
            """;
    }
}
