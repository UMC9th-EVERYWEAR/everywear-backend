package com.umc.EveryWear.domain.product.service.query;

import com.umc.EveryWear.domain.product.converter.ProductConverter;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {

    private final UserProductRepository userProductRepository;

    @Override
    public ProductResDTO.ProductListResponse getAllProductsByUserId(Long userId) {
        List<UserProduct> userProducts = userProductRepository.findAllProductsByUserIdOrderByUpdatedAtDesc(userId);
        return toProductListResponse(userProducts);
    }

    @Override
    public ProductResDTO.ProductListResponse getProductsByCategory(Long userId, String category) {
        List<UserProduct> userProducts = userProductRepository.findProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, category);
        return toProductListResponse(userProducts);
    }

    private static ProductResDTO.ProductListResponse toProductListResponse(List<UserProduct> userProducts) {
        List<ProductResDTO.ListDTO> products = userProducts.stream()
                .map(ProductConverter::toListDTO)
                .collect(Collectors.toList());
        return ProductResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }
}
