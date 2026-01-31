package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;

/**
 * FittingStrategy 인터페이스는 피팅 전략을 정의합니다.
 * 각 피팅 전략은 특정 카테고리를 지원하며, 해당 카테고리에 대한 피팅 지침을 제공합니다.
 */
public interface FittingStrategy {
    boolean supports(ClothingCategory category);
    String buildPrompt();
}
