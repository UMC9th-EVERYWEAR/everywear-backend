package com.umc.EveryWear.domain.home.service.query;

import com.umc.EveryWear.domain.home.converter.HomeConverter;
import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeQueryServiceImpl implements HomeQueryService {

    private static final int HOME_PRODUCT_LIMIT = 6;

    private final UserProductRepository userProductRepository;

    @Override
    public HomeResDTO.ProductListResponse getHomeProducts(Long userId) {
        Pageable pageable = PageRequest.of(0, HOME_PRODUCT_LIMIT);
        List<UserProduct> userProducts = userProductRepository.findTop6ByUserIdOrderByUpdatedAtDesc(userId, pageable);
        List<HomeResDTO.ProductDTO> products = userProducts.stream()
                .map(HomeConverter::toProductDTO)
                .collect(Collectors.toList());
        return HomeResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }
}
