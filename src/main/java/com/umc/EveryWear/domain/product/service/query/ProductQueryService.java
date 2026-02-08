package com.umc.EveryWear.domain.product.service.query;

import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;

public interface ProductQueryService {
    ProductResDTO.ProductListResponse getAllProducts(Long userId);
    ProductResDTO.ProductListResponse getProductsByCategory(Long userId, String category);
    ProductResDTO.ProductForFittingResponse getProductForFitting(Long userId, Long productId);
}
