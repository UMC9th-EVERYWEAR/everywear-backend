package com.umc.EveryWear.domain.closet.service.query;

import com.umc.EveryWear.domain.closet.dto.res.ClosetResDTO;

public interface ClosetQueryService {

    ClosetResDTO.ProductListResponse getClosetProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetTopProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetBottomProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetOuterProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetDressProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetEtcProducts(Long userId);
}
