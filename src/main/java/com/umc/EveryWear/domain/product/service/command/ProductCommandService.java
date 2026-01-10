package com.umc.EveryWear.domain.product.service.command;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;

public interface ProductCommandService {
    ProductResDTO.CrawlingDTO crawlAndSaveMusinsaProduct(ProductReqDTO.CrawlingDTO dto);
}
