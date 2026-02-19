package com.umc.EveryWear.domain.product.service.command;

import com.umc.EveryWear.domain.product.converter.ProductConverter;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.exception.ProductException;
import com.umc.EveryWear.domain.product.exception.code.ProductErrorCode;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductImportTransactionService {

    private final ProductRepository productRepository;
    private final UserProductRepository userProductRepository;

    @Transactional
    public ProductResDTO.ImportDTO inUserProduct(Long userId, User user, Product product) {
        UserProduct up = userProductRepository.findByUser_UserIdAndProduct_ProductId(userId, product.getProductId()).orElse(null);
        if (up == null) {
            UserProduct saved = userProductRepository.save(UserProduct.builder().user(user).product(product).build());
            return ProductConverter.toImportDTO(saved, true, false);
        }
        userProductRepository.updateUpdatedAt(userId, product.getProductId(), LocalDateTime.now());
        return ProductConverter.toImportDTO(up, true, false);
    }

    @Transactional
    public ProductResDTO.ImportDTO createProductAndUserProduct(User user, ProductCrawlingData data) {
        Product product = Product.builder()
                .shoppingmallName(data.getShoppingmallName())
                .productUrl(data.getProductUrl())
                .category(data.getCategory())
                .productImgUrl(data.getProductImgUrl())
                .productName(data.getProductName())
                .brandName(data.getBrandName())
                .price(data.getPrice())
                .starPoint(data.getStarPoint())
                .aiReview(data.getAiReview())
                .productNum(data.getProductNum())
                .build();
        Product savedProduct = productRepository.save(product);
        UserProduct savedUp = userProductRepository.save(UserProduct.builder().user(user).product(savedProduct).build());
        return ProductConverter.toImportDTO(savedUp, false, false);
    }

    @Transactional
    public ProductResDTO.ImportDTO inProductNum(Long userId, User user, Long productNum) {
        Product existing = productRepository.findByProductNum(productNum)
                .orElseThrow(() -> new ProductException(ProductErrorCode.CRAWLING_FAILED));
        productRepository.updateUpdatedAt(existing.getProductId());
        return inUserProduct(userId, user, existing);
    }
}
