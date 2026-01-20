package com.umc.EveryWear.domain.home.service.query;

import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;

public interface HomeQueryService {
    HomeResDTO.ProductListResponse getHomeProducts(Long userId);
}
