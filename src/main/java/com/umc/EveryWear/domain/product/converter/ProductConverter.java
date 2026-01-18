package com.umc.EveryWear.domain.product.converter;

import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;

public class ProductConverter {

    // Entity -> DTO (상품 등록 응답)
    public static ProductResDTO.ImportDTO toImportDTO(Product product) {
        return toImportDTO(product, false);
    }

    // Entity -> DTO (상품 등록 응답, 업데이트 여부 포함)
    public static ProductResDTO.ImportDTO toImportDTO(Product product, boolean isUpdated) {
        return toImportDTO(product, isUpdated, false);
    }

    // Entity -> DTO (상품 등록 응답, 업데이트 여부 및 URL 업데이트 여부 포함)
    public static ProductResDTO.ImportDTO toImportDTO(Product product, boolean isUpdated, boolean isUrlUpdated) {
        return ProductResDTO.ImportDTO.builder()
                .product_id(product.getProductId())
                .shoppingmale_name(product.getShoppingmallName())
                .product_url(product.getProductUrl())
                .category(product.getCategory())
                .product_img_url(product.getProductImgUrl())
                .product_name(product.getProductName())
                .brand_name(product.getBrandName())
                .price(product.getPrice())
                .star_point(product.getStarPoint())
                .AI_review(product.getAiReview())
                .product_num(product.getProductNum())
                .isUpdated(isUpdated)
                .isUrlUpdated(isUrlUpdated)
                .build();
    }
}
