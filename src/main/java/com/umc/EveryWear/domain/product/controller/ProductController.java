package com.umc.EveryWear.domain.product.controller;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.enums.ProductCategory;
import com.umc.EveryWear.domain.product.enums.ShoppingMall;
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
            description = "등록할 상품 url을 입력해 상품 정보를 저장합니다.\n\n" +
                    "무신사 입력 URL 형식들 : \n\n" +
                    " • 웹 URL: https://www.musinsa.com/products/{상품ID}\n\n" +
                    " • 앱 URL: https://www.musinsa.com/products/{상품ID}\n\n" +
                    " • 웹 공유하기: https://musinsa.onelink.me/ANAQ/{공유코드}\n\n" +
                    " • 앱 공유하기: https://musinsa.onelink.me/ANAQ/{공유코드}\n\n" +
                    " • 쇼핑몰 앱 공유하기: https://musinsa.onelink.me/PvkC/{공유코드}\n\n" +
                    "지그재그 입력 URL 형식들 : \n\n" +
                    " • 웹 URL: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 앱 URL: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 웹 공유하기: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 앱 공유하기: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 쇼핑몰 앱 공유하기: https://s.zigzag.kr/{공유코드}\n\n" +
                    "W컨셉 입력 URL 형식들 : \n\n" +
                    " • 웹 URL: https://www.wconcept.co.kr/Product/{상품ID}?entry_channel=...\n\n" +
                    " • 앱 URL: https://m.wconcept.co.kr/Product/{상품ID}?applanding=X\n\n" +
                    " • 웹 공유하기: https://www.wconcept.co.kr/Product/{상품ID}?applanding=Y\n\n" +
                    " • 앱 공유하기: https://m.wconcept.co.kr/Product/{상품ID}?applanding=Z}\n\n" +
                    " • 쇼핑몰 앱 공유하기: https://m.wconcept.co.kr/Product/{상품ID}?applanding=Y\n\n" +
                    "29cm 입력 URL 형식들 : \n\n" +
                    " • 웹 URL: https://www.29cm.co.kr/products/{상품ID}?categoryLargeCode=...\n\n" +
                    " • 앱 URL: https://www.29cm.co.kr/products/{상품ID}\n\n" +
                    " • 웹 공유하기: https://29cm.onelink.me/1080201211/{공유코드}\n\n" +
                    " • 앱 공유하기: https://29cm.onelink.me/1080201211/{공유코드}\n\n" +
                    " • 쇼핑몰 앱 공유하기: https://29cm.onelink.me/1080201211/{공유코드}"
    )
    @PostMapping("/product/import")
    public ApiResponse<ProductResDTO.ImportDTO> importProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.ImportDTO dto
    ) {
        ProductResDTO.ImportResult result = productCommandService.importProduct(userId, dto);
        ProductSuccessCode successCode = getImportSuccessCode(result.getMall(), result.getDto());
        return ApiResponse.onSuccess(successCode, result.getDto());
    }

    private ProductSuccessCode getImportSuccessCode(ShoppingMall mall, ProductResDTO.ImportDTO dto) {
        boolean urlUpdated = Boolean.TRUE.equals(dto.getIsUrlUpdated());
        boolean updated = Boolean.TRUE.equals(dto.getIsUpdated());
        if (urlUpdated) {
            return switch (mall) {
                case MUSINSA -> ProductSuccessCode.MUSINSA_PRODUCT_URL_UPDATED;
                case ZIGZAG -> ProductSuccessCode.ZIGZAG_PRODUCT_URL_UPDATED;
                case CM29 -> ProductSuccessCode.CM29_PRODUCT_URL_UPDATED;
                case WCONCEPT -> ProductSuccessCode.WCONCEPT_PRODUCT_URL_UPDATED;
            };
        }
        if (updated) {
            return switch (mall) {
                case MUSINSA -> ProductSuccessCode.MUSINSA_PRODUCT_UPDATED;
                case ZIGZAG -> ProductSuccessCode.ZIGZAG_PRODUCT_UPDATED;
                case CM29 -> ProductSuccessCode.CM29_PRODUCT_UPDATED;
                case WCONCEPT -> ProductSuccessCode.WCONCEPT_PRODUCT_UPDATED;
            };
        }
        return switch (mall) {
            case MUSINSA -> ProductSuccessCode.MUSINSA_PRODUCT_ADDED;
            case ZIGZAG -> ProductSuccessCode.ZIGZAG_PRODUCT_ADDED;
            case CM29 -> ProductSuccessCode.CM29_PRODUCT_ADDED;
            case WCONCEPT -> ProductSuccessCode.WCONCEPT_PRODUCT_ADDED;
        };
    }

    @Operation(
            summary = "상품 단건 조회",
            description = "사용자가 선택한 상품의 정보를 조회합니다."
    )
    @GetMapping("/products/{product_id}")
    public ApiResponse<ProductResDTO.ProductDetailResponse> getProductDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("product_id") Long productId
    ) {
        ProductResDTO.ProductDetailResponse response = productQueryService.getProductDetail(userId, productId);
        return ApiResponse.onSuccess(ProductSuccessCode.PRODUCT_FITTING_200, response);
    }

    @Operation(
            summary = "상품 좋아요 토글",
            description = "특정 상품에 대한 사용자의 좋아요 상태를 토글합니다. 호출할 때마다 true/false가 반전됩니다."
    )
    @PatchMapping("/products/{product_id}/like")
    public ApiResponse<ProductResDTO.LikeToggleDTO> toggleProductLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable("product_id") Long productId
    ) {
        ProductResDTO.LikeToggleDTO result = productCommandService.toggleProductLike(userId, productId);
        ProductSuccessCode successCode = Boolean.TRUE.equals(result.getIs_liked())
                ? ProductSuccessCode.PRODUCT_LIKE_ENABLED
                : ProductSuccessCode.PRODUCT_LIKE_DISABLED;
        return ApiResponse.onSuccess(successCode, result);
    }
}
