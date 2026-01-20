package com.umc.EveryWear.domain.home.converter;

import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.product.entity.Product;

public class HomeConverter {

    // Product Entity -> Home ProductDTO
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
                .build();
    }
}
