package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import org.springframework.stereotype.Component;

@Component
public class DressFittingStrategy implements FittingStrategy {

    @Override
    public boolean supports(ClothingCategory category) {
        return category == ClothingCategory.DRESS;
    }

    @Override
    public String buildPrompt() {
        return """
            Replace the person's entire outfit with the provided dress image.
            The dress should cover the upper and lower body naturally as a single garment.
            Maintain realistic proportions, fabric flow, folds, and lighting.
            Preserve the person's pose, face, hairstyle, and background.
            Do not alter body shape or camera perspective.
            """;
    }
}