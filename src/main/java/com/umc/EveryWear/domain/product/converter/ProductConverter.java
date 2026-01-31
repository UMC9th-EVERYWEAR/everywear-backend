package com.umc.EveryWear.domain.product.converter;

import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;

public class ProductConverter {

    // Entity -> DTO (신규 상품 등록 응답)
    public static ProductResDTO.ImportDTO toImportDTO(Product product) {
        return toImportDTO(product, false);
    }

    // Entity -> DTO (신규 상품 등록 응답, 업데이트 여부 포함)
    public static ProductResDTO.ImportDTO toImportDTO(Product product, boolean isUpdated) {
        return toImportDTO(product, isUpdated, false);
    }

    // Entity -> DTO (신규 상품 등록 응답, 업데이트 여부 및 URL 업데이트 여부 포함)
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
                .is_liked(false) // Product만 있는 컨텍스트에서는 is_liked를 알 수 없으므로 기본값
                .isUpdated(isUpdated)
                .isUrlUpdated(isUrlUpdated)
                .build();
    }

    // Entity -> DTO (상품 재등록 응답)
    public static ProductResDTO.ImportDTO toImportDTO(UserProduct userProduct) {
        return toImportDTO(userProduct, false, false);
    }

    // Entity -> DTO (상품 재등록 응답, 업데이트 여부 포함)
    public static ProductResDTO.ImportDTO toImportDTO(UserProduct userProduct, boolean isUpdated) {
        return toImportDTO(userProduct, isUpdated, false);
    }

    // Entity -> DTO (상품 재등록 응답, 업데이트 여부 및 URL 업데이트 여부 포함)
    public static ProductResDTO.ImportDTO toImportDTO(UserProduct userProduct, boolean isUpdated, boolean isUrlUpdated) {
        Product product = userProduct.getProduct();
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
                .is_liked(userProduct.getIsLiked())
                .isUpdated(isUpdated)
                .isUrlUpdated(isUrlUpdated)
                .build();
    }

    // Entity -> DTO (상품 조회 응답) - UserProduct 기반
    public static ProductResDTO.ListDTO toListDTO(UserProduct userProduct) {
        Product product = userProduct.getProduct();
        return ProductResDTO.ListDTO.builder()
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
                .is_liked(userProduct.getIsLiked())
                .build();
    }
}
