package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

@Component
public class OuterFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.OUTER;
    }

    @Override
    public String buildPrompt() {
        return """
            Replace the outerwear of the person with the provided garment image.
            The outer garment should be layered naturally over the existing outfit.
            Preserve the inner clothing, body proportions, pose, lighting, and shadows.
            Do not modify the face, background, or body shape.
            Ensure realistic overlap, folds, and depth for the outerwear.
            """;
    }
}