package com.umc.EveryWear.domain.product.service.command;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;

public interface ProductCommandService {
    ProductResDTO.ImportResult importProduct(Long userId, ProductReqDTO.ImportDTO dto);
    ProductResDTO.ImportDTO importMusinsaProduct(Long userId, ProductReqDTO.ImportMusinsaDTO dto);
    ProductResDTO.ImportDTO importZigzagProduct(Long userId, ProductReqDTO.ImportZigzagDTO dto);
    ProductResDTO.ImportDTO importWconceptProduct(Long userId, ProductReqDTO.ImportWconceptDTO dto);
    ProductResDTO.ImportDTO import29cmProduct(Long userId, ProductReqDTO.Import29cmDTO dto);
    ProductResDTO.LikeToggleDTO toggleProductLike(Long userId, Long productId);
}
