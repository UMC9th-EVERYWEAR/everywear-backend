package com.umc.EveryWear.domain.product.controller;

import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.exception.code.ProductSuccessCode;
import com.umc.EveryWear.domain.product.service.command.ProductCommandService;
import com.umc.EveryWear.domain.product.service.query.ProductQueryService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/product")
    public ApiResponse<ProductResDTO.ProductListResponse> getAllProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ProductResDTO.ProductListResponse response = productQueryService.getAllProductsByUserId(userId);
        return ApiResponse.onSuccess(ProductSuccessCode.PRODUCTS_RETRIEVED, response);
    }

    @Operation(
            summary = "무신사 상품 등록",
            description = "등록할 상품 url을 입력해 상품 정보를 저장합니다.\n\n" +
                    "입력 URL 형식들 : \n\n" +
                    " • 웹 URL: https://www.musinsa.com/products/{상품ID}\n\n" +
                    " • 앱 URL: https://www.musinsa.com/products/{상품ID}\n\n" +
                    " • 웹 공유하기: https://musinsa.onelink.me/ANAQ/{공유코드}\n\n" +
                    " • 앱 공유하기: https://musinsa.onelink.me/ANAQ/{공유코드}\n\n" +
                    " • 쇼핑몰 앱 공유하기: https://musinsa.onelink.me/PvkC/{공유코드}"
    )
    @PostMapping("/product/import/musinsa")
    public ApiResponse<ProductResDTO.ImportDTO> importMusinsaProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.ImportMusinsaDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.importMusinsaProduct(userId, dto);
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

    @Operation(
            summary = "지그재그 상품 등록",
            description = "등록할 상품 url을 입력해 상품 정보를 저장합니다.\n\n" +
                    "입력 URL 형식들 : \n\n" +
                    " • 웹 URL: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 앱 URL: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 웹 공유하기: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 앱 공유하기: https://zigzag.kr/catalog/products/{상품ID}\n\n" +
                    " • 쇼핑몰 앱 공유하기: https://s.zigzag.kr/{공유코드}"
    )
    @PostMapping("/product/import/zigzag")
    public ApiResponse<ProductResDTO.ImportDTO> importZigzagProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.ImportZigzagDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.importZigzagProduct(userId, dto);
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

    @Operation(
            summary = "W컨셉 상품 등록",
            description = "등록할 상품 url을 입력해 상품 정보를 저장합니다.\n\n" +
                    "입력 URL 형식들 : \n\n" +
                    " • 웹 URL: https://www.wconcept.co.kr/Product/{상품ID}?entry_channel=...\n\n" +
                    " • 앱 URL: https://m.wconcept.co.kr/Product/{상품ID}?applanding=X\n\n" +
                    " • 웹 공유하기: https://www.wconcept.co.kr/Product/{상품ID}?applanding=Y\n\n" +
                    " • 앱 공유하기: https://m.wconcept.co.kr/Product/{상품ID}?applanding=Z}\n\n" +
                    " • 쇼핑몰 앱 공유하기: https://m.wconcept.co.kr/Product/{상품ID}?applanding=Y"
    )
    @PostMapping("/product/import/wconcept")
    public ApiResponse<ProductResDTO.ImportDTO> importWconceptProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.WconceptImportDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.importWconceptProduct(userId, dto);
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

    @Operation(
        summary = "29cm 상품 등록", 
        description = "등록할 상품 url을 입력해 상품 정보를 저장합니다.\n\n" +
            "입력 URL 형식들 : \n\n" +
            " • 웹 URL: https://www.29cm.co.kr/products/{상품ID}?categoryLargeCode=...\n\n" +
            " • 앱 URL: https://www.29cm.co.kr/products/{상품ID}\n\n" +
            " • 웹 공유하기: https://29cm.onelink.me/1080201211/{공유코드}\n\n" +
            " • 앱 공유하기: https://29cm.onelink.me/1080201211/{공유코드}\n\n" +
            " • 쇼핑몰 앱 공유하기: https://29cm.onelink.me/1080201211/{공유코드}"
    )
    @PostMapping("/product/import/29cm")
    public ApiResponse<ProductResDTO.ImportDTO> import29cmProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductReqDTO.Import29cmDTO dto
    ) {
        ProductResDTO.ImportDTO response = productCommandService.import29cmProduct(userId, dto);
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
