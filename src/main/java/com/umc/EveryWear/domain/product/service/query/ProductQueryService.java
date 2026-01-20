package com.umc.EveryWear.domain.product.service.query;

import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;

public interface ProductQueryService {
    ProductResDTO.ProductListResponse getAllProductsByUserId(Long userId);
    ProductResDTO.ProductListResponse getProductsByCategory(Long userId, String category);
    ProductResDTO.ProductListResponse getHomeProducts(Long userId);
}
