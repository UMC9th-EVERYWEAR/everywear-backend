package com.umc.EveryWear.domain.home.converter;

import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;

public class HomeConverter {

    // Product Entity -> Home ProductDTO (fallback)
    public static HomeResDTO.ProductDTO toProductDTO(Product product) {
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
                .is_liked(false) // Product만 있는 경우 기본값
                .build();
    }

    // UserProduct Entity -> Home ProductDTO (실제 is_liked 사용)
    public static HomeResDTO.ProductDTO toProductDTO(UserProduct userProduct) {
        Product product = userProduct.getProduct();
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
                .is_liked(userProduct.getIsLiked())
                .build();
    }
}
