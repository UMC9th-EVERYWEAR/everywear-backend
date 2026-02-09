package com.umc.EveryWear.domain.product.service.command;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;

public interface ProductCommandService {
    ProductResDTO.ImportResult importProduct(Long userId, ProductReqDTO.ImportDTO dto);
    ProductResDTO.ImportResult importMusinsaProduct(Long userId, ProductReqDTO.ImportMusinsaDTO dto);
    ProductResDTO.ImportResult importZigzagProduct(Long userId, ProductReqDTO.ImportZigzagDTO dto);
    ProductResDTO.ImportResult importWconceptProduct(Long userId, ProductReqDTO.ImportWconceptDTO dto);
    ProductResDTO.ImportResult import29cmProduct(Long userId, ProductReqDTO.Import29cmDTO dto);
    ProductResDTO.ImportResult getImportStatus(Long userId, Long jobId);
    ProductResDTO.LikeToggleDTO toggleProductLike(Long userId, Long productId);
}
