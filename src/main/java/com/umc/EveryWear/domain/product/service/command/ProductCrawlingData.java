package com.umc.EveryWear.domain.product.service.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCrawlingData {
    private String shoppingmallName;
    private String productUrl;
    private String category;
    private String productImgUrl;
    private String productName;
    private String brandName;
    private String price;
    private Float starPoint;
    private String aiReview;
    private Long productNum;
}
