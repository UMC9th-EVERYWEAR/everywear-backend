package com.umc.EveryWear.domain.product.controller;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.enums.ProductCategory;
import com.umc.EveryWear.domain.product.exception.code.ProductSuccessCode;
import com.umc.EveryWear.domain.product.service.command.ProductCommandService;
import com.umc.EveryWear.domain.product.service.query.ProductQueryService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product", description = "상품 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    @Operation(
            summary = "전체 상품 조회",
            description = "사용자가 등록한 모든 상품을 최신 업데이트 순으로 조회합니다."
    )
    @GetMapping("/products")
    public ApiResponse<ProductResDTO.ProductListResponse> getProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ProductResDTO.ProductListResponse response = productQueryService.getAllProducts(userId);
        return ApiResponse.onSuccess(ProductSuccessCode.PRODUCTS_RETRIEVED, response);
    }

    @Operation(summary = "상의 상품 조회", description = "사용자가 등록한 상의 상품을 최신 업데이트 순으로 조회합니다.")
    @GetMapping("/products/top")
    public ApiResponse<ProductResDTO.ProductListResponse> getTopProducts(@AuthenticationPrincipal Long userId) {
        return getCategoryProductsResponse(userId, ProductCategory.TOP);
    }

    @Operation(summary = "하의 상품 조회", description = "사용자가 등록한 하의 상품을 최신 업데이트 순으로 조회합니다.")
    @GetMapping("/products/bottom")
    public ApiResponse<ProductResDTO.ProductListResponse> getBottomProducts(@AuthenticationPrincipal Long userId) {
        return getCategoryProductsResponse(userId, ProductCategory.BOTTOM);
    }

    @Operation(summary = "아우터 상품 조회", description = "사용자가 등록한 아우터 상품을 최신 업데이트 순으로 조회합니다.")
    @GetMapping("/products/outer")
    public ApiResponse<ProductResDTO.ProductListResponse> getOuterProducts(@AuthenticationPrincipal Long userId) {
        return getCategoryProductsResponse(userId, ProductCategory.OUTER);
    }

    @Operation(summary = "원피스 상품 조회", description = "사용자가 등록한 원피스 상품을 최신 업데이트 순으로 조회합니다.")
    @GetMapping("/products/dress")
    public ApiResponse<ProductResDTO.ProductListResponse> getDressProducts(@AuthenticationPrincipal Long userId) {
        return getCategoryProductsResponse(userId, ProductCategory.DRESS);
    }

    @Operation(summary = "기타 상품 조회", description = "사용자가 등록한 기타 상품을 최신 업데이트 순으로 조회합니다.")
    @GetMapping("/products/etc")
    public ApiResponse<ProductResDTO.ProductListResponse> getEtcProducts(@AuthenticationPrincipal Long userId) {
        return getCategoryProductsResponse(userId, ProductCategory.ETC);
    }

    private ApiResponse<ProductResDTO.ProductListResponse> getCategoryProductsResponse(Long userId, ProductCategory category) {
        ProductResDTO.ProductListResponse response = productQueryService.getProductsByCategory(userId, category.getValue());
        return ApiResponse.onSuccess(category.getSuccessCode(), response);
    }

    @Operation(
            summary = "상품 등록",
            description = "상품 URL로 무신사/지그재그/29cm/W컨셉 자동 구분 후 크롤링·저장. 지원: musinsa.com, musinsa.onelink.me / zigzag.kr, s.zigzag.kr / 29cm.co.kr, 29cm.onelink.me / wconcept.co.kr, m.wconcept.co.kr"
    )
    @PostMapping("/product/import")
    public ApiResponse<ProductResDTO.ImportDTO> importProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.ImportDTO dto
    ) {
        ProductResDTO.ImportResult result = productCommandService.importProduct(userId, dto);
        ProductResDTO.ImportDTO dtoResult = result.getDto();
        ProductSuccessCode successCode = ProductSuccessCode.forImportResult(
                result.getMall(),
                Boolean.TRUE.equals(dtoResult.getIsUrlUpdated()),
                Boolean.TRUE.equals(dtoResult.getIsUpdated())
        );
        return ApiResponse.onSuccess(successCode, dtoResult);
    }

    @Operation(summary = "상품 좋아요 토글", description = "특정 상품의 좋아요 상태를 토글합니다.")
    @PatchMapping("/products/{product_id}/like")
    public ApiResponse<ProductResDTO.LikeToggleDTO> toggleProductLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable("product_id") Long productId
    ) {
        ProductResDTO.LikeToggleDTO result = productCommandService.toggleProductLike(userId, productId);
        return ApiResponse.onSuccess(ProductSuccessCode.forLikeToggle(result.getIs_liked()), result);
    }
}
