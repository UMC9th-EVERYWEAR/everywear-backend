package com.umc.EveryWear.domain.product.controller;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.exception.code.ProductSuccessCode;
import com.umc.EveryWear.domain.product.service.command.ProductCommandService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 관련 API")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandService productCommandService;

    @Operation(summary = "무신사 상품 등록", description = "등록할 상품 url을 받아 DB에 상품 정보를 저장합니다.")
    @PostMapping("/crawling/musinsa")
    public ApiResponse<ProductResDTO.CrawlingDTO> crawlMusinsaProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.CrawlingDTO dto
    ) {
        ProductResDTO.CrawlingDTO response = productCommandService.crawlAndSaveMusinsaProduct(dto);
        return ApiResponse.onSuccess(ProductSuccessCode.MUSINSA_PRODUCT_ADDED, response);
    }
}
