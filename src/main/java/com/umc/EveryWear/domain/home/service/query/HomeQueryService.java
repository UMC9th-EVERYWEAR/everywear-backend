package com.umc.EveryWear.domain.home.service.query;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;

import java.util.List;

public interface HomeQueryService {
    HomeResDTO.ProductListResponse getHomeProducts(Long userId);

    List<FittingResponseDto.FittingSummary> getRecentFittings(Long userId);
}
