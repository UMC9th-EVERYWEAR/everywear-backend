package com.umc.EveryWear.domain.product.converter;

import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;

public class ProductConverter {

    public static ProductResDTO.ImportDTO toImportDTO(Product product) {
        return toImportDTO(product, false);
    }

    public static ProductResDTO.ImportDTO toImportDTO(Product product, boolean isUpdated) {
        return toImportDTO(product, isUpdated, false);
    }

    public static ProductResDTO.ImportDTO toImportDTO(Product product, boolean isUpdated, boolean isUrlUpdated) {
        return buildImportDTO(product, false, isUpdated, isUrlUpdated);
    }

    public static ProductResDTO.ImportDTO toImportDTO(UserProduct userProduct) {
        return toImportDTO(userProduct, false, false);
    }

    public static ProductResDTO.ImportDTO toImportDTO(UserProduct userProduct, boolean isUpdated, boolean isUrlUpdated) {
        return buildImportDTO(userProduct.getProduct(), userProduct.getIsLiked(), isUpdated, isUrlUpdated);
    }

    private static ProductResDTO.ImportDTO buildImportDTO(Product product, Boolean isLiked, boolean isUpdated, boolean isUrlUpdated) {
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
                .is_liked(isLiked != null ? isLiked : false)
                .isUpdated(isUpdated)
                .isUrlUpdated(isUrlUpdated)
                .build();
    }

    public static ProductResDTO.ListDTO toListDTO(UserProduct userProduct) {
        return buildListDTO(userProduct.getProduct(), userProduct.getIsLiked());
    }

    private static ProductResDTO.ListDTO buildListDTO(Product product, Boolean isLiked) {
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
                .is_liked(isLiked != null ? isLiked : false)
                .build();
    }

    public static ProductResDTO.ProductForFittingDTO toProductForFittingDTO(UserProduct userProduct) {
        Product product = userProduct.getProduct();
        Boolean isLiked = userProduct.getIsLiked();
        return ProductResDTO.ProductForFittingDTO.builder()
                .AI_review(product.getAiReview())
                .brand_name(product.getBrandName())
                .category(product.getCategory())
                .is_liked(isLiked != null ? isLiked : false)
                .price(product.getPrice())
                .product_id(product.getProductId())
                .product_img_url(product.getProductImgUrl())
                .product_name(product.getProductName())
                .product_num(product.getProductNum())
                .product_url(product.getProductUrl())
                .shoppingmale_name(product.getShoppingmallName())
                .star_point(product.getStarPoint())
                .build();
    }
}
