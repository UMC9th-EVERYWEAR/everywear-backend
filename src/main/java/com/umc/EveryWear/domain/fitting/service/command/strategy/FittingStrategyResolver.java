package com.umc.EveryWear.domain.fitting.service.command.strategy;

import com.umc.EveryWear.domain.fitting.enums.ClothingCategory;
import com.umc.EveryWear.domain.fitting.exception.FittingException;
import com.umc.EveryWear.domain.fitting.exception.code.FittingErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FittingStrategyResolver {

    private final List<FittingStrategy> strategies;

    public FittingStrategy find(ClothingCategory category) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(category))
                .findFirst()
                .orElseThrow(() ->
                        new FittingException(FittingErrorCode.INVALID_PRODUCT_CATEGORY)
                );
    }
}
