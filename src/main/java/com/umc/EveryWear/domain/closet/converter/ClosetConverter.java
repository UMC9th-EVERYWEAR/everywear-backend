package com.umc.EveryWear.domain.closet.converter;

import com.umc.EveryWear.domain.closet.dto.res.ClosetResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;

public class ClosetConverter {

    public static ClosetResDTO.ProductDTO toProductDTO(UserProduct userProduct, Long recentFittingId) {
        Product product = userProduct.getProduct();
        return ClosetResDTO.ProductDTO.builder()
                .AI_review(product.getAiReview())
                .brand_name(product.getBrandName())
                .category(product.getCategory())
                .is_liked(userProduct.getIsLiked())
                .price(product.getPrice())
                .product_id(product.getProductId())
                .product_img_url(product.getProductImgUrl())
                .product_name(product.getProductName())
                .product_num(product.getProductNum())
                .product_url(product.getProductUrl())
                .shoppingmale_name(product.getShoppingmallName())
                .star_point(product.getStarPoint())
                .recent_fitting_id(recentFittingId)
                .build();
    }
}
