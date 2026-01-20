package com.umc.EveryWear.domain.home.service.query;

import com.umc.EveryWear.domain.home.converter.HomeConverter;
import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeQueryServiceImpl implements HomeQueryService {

    private final ProductRepository productRepository;

    @Override
    public HomeResDTO.ProductListResponse getHomeProducts(Long userId) {
        List<Product> products = productRepository.findTop6ByUser_UserIdOrderByUpdatedAtDesc(userId);
        
        List<HomeResDTO.ProductDTO> productList = products.stream()
                .map(HomeConverter::toProductDTO)
                .collect(Collectors.toList());

        return HomeResDTO.ProductListResponse.builder()
                .products(productList)
                .build();
    }
}
