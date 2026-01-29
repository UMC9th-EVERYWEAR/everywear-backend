package com.umc.EveryWear.domain.product.service.query;

import com.umc.EveryWear.domain.product.converter.ProductConverter;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
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
        List<Product> products = userProductRepository.findAllProductsByUserIdOrderByUpdatedAtDesc(userId);
        
        List<ProductResDTO.ListDTO> productList = products.stream()
                .map(ProductConverter::toListDTO)
                .collect(Collectors.toList());

        return ProductResDTO.ProductListResponse.builder()
                .products(productList)
                .build();
    }

    @Override
    public ProductResDTO.ProductListResponse getProductsByCategory(Long userId, String category) {
        List<Product> products = userProductRepository.findProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, category);
        
        List<ProductResDTO.ListDTO> productList = products.stream()
                .map(ProductConverter::toListDTO)
                .collect(Collectors.toList());

        return ProductResDTO.ProductListResponse.builder()
                .products(productList)
                .build();
    }
}
