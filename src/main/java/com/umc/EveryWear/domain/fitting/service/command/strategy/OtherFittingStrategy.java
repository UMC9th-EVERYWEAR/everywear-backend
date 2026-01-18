package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

@Component
public class OtherFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.OTHER;
    }

    @Override
    public String buildPrompt() {
        return """
            Replace the most relevant clothing area of the person with the provided garment image.
            Adapt the placement of the garment naturally based on the person's pose and body structure.
            Preserve realistic proportions, lighting, shadows, and fabric appearance.
            Do not modify the face, background, or body shape.
            Ensure the garment blends naturally with existing clothing if partial overlap is required.
            """;
    }
}