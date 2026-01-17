package com.umc.EveryWear.domain.product.controller;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.exception.code.ProductSuccessCode;
import com.umc.EveryWear.domain.product.service.command.ProductCommandService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 관련 API")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandService productCommandService;

    @Operation(summary = "무신사 상품 등록", description = "등록할 상품 url을 받아 DB에 상품 정보를 저장합니다.")
    @PostMapping("/import/musinsa")
    public ApiResponse<ProductResDTO.ImportDTO> importMusinsaProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.ImportMusinsaDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.importMusinsaProduct(dto);
        ProductSuccessCode successCode;
        if (response.getIsUrlUpdated() != null && response.getIsUrlUpdated()) {
            successCode = ProductSuccessCode.MUSINSA_PRODUCT_URL_UPDATED;
        } else if (response.getIsUpdated() != null && response.getIsUpdated()) {
            successCode = ProductSuccessCode.MUSINSA_PRODUCT_UPDATED;
        } else {
            successCode = ProductSuccessCode.MUSINSA_PRODUCT_ADDED;
        }
        return ApiResponse.onSuccess(successCode, response);
    }

    @Operation(summary = "지그재그 상품 등록", description = "등록할 상품 url을 받아 DB에 상품 정보를 저장합니다.")
    @PostMapping("/import/zigzag")
    public ApiResponse<ProductResDTO.ImportDTO> importZigzagProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.ImportZigzagDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.importZigzagProduct(dto);
        ProductSuccessCode successCode;
        if (response.getIsUrlUpdated() != null && response.getIsUrlUpdated()) {
            successCode = ProductSuccessCode.ZIGZAG_PRODUCT_URL_UPDATED;
        } else if (response.getIsUpdated() != null && response.getIsUpdated()) {
            successCode = ProductSuccessCode.ZIGZAG_PRODUCT_UPDATED;
        } else {
            successCode = ProductSuccessCode.ZIGZAG_PRODUCT_ADDED;
        }
        return ApiResponse.onSuccess(successCode, response);
        }

    @Operation(summary = "W컨셉 상품 등록", description = "등록할 상품 url을 받아 DB에 상품 정보를 저장합니다.")
    @PostMapping("/import/wconcept")
    public ApiResponse<ProductResDTO.ImportDTO> importWconceptProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.WconceptImportDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.importWconceptProduct(dto);
        ProductSuccessCode successCode;
        if (response.getIsUrlUpdated() != null && response.getIsUrlUpdated()) {
            successCode = ProductSuccessCode.WCONCEPT_PRODUCT_URL_UPDATED;
        } else if (response.getIsUpdated() != null && response.getIsUpdated()) {
            successCode = ProductSuccessCode.WCONCEPT_PRODUCT_UPDATED;
        } else {
            successCode = ProductSuccessCode.WCONCEPT_PRODUCT_ADDED;
        }
        return ApiResponse.onSuccess(successCode, response);
        }

    @Operation(summary = "29cm 상품 등록", description = "등록할 상품 url을 받아 DB에 상품 정보를 저장합니다.")
    @PostMapping("/import/29cm")
    public ApiResponse<ProductResDTO.ImportDTO> import29cmProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.Import29cmDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.import29cmProduct(dto);
        ProductSuccessCode successCode;
        if (response.getIsUrlUpdated() != null && response.getIsUrlUpdated()) {
            successCode = ProductSuccessCode.CM29_PRODUCT_URL_UPDATED;
        } else if (response.getIsUpdated() != null && response.getIsUpdated()) {
            successCode = ProductSuccessCode.CM29_PRODUCT_UPDATED;
        } else {
            successCode = ProductSuccessCode.CM29_PRODUCT_ADDED;
        }
        return ApiResponse.onSuccess(successCode, response);
    }
}
