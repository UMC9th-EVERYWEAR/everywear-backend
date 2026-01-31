package com.umc.EveryWear.domain.closet.service.query;

import com.umc.EveryWear.domain.closet.dto.res.ClosetResDTO;

public interface ClosetQueryService {

    ClosetResDTO.ProductListResponse getClosetProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetTopProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetBottomProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetOuterProducts(Long userId);

    ClosetResDTO.ProductListResponse getClosetDressProducts(Long userId);

    /**
     * 내 옷장 기타 상품 조회 (is_liked = true, category = "기타", update_at 내림차순)
     */
    ClosetResDTO.ProductListResponse getClosetEtcProducts(Long userId);
}
