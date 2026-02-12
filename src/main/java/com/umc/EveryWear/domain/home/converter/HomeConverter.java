package com.umc.EveryWear.domain.home.converter;

import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;

public final class HomeConverter {

    private HomeConverter() {}

    public static HomeResDTO.ProductDTO toProductDTO(UserProduct userProduct) {
        return buildProductDTO(userProduct.getProduct(), userProduct.getIsLiked());
    }

    private static HomeResDTO.ProductDTO buildProductDTO(Product product, Boolean isLiked) {
        return HomeResDTO.ProductDTO.builder()
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
}
