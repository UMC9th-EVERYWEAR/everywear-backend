package com.umc.EveryWear.domain.home.service.query;

import com.umc.EveryWear.domain.home.converter.HomeConverter;
import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.fitting.repository.FittingRepository;
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

    private final FittingRepository fittingRepository;

    @Override
    public HomeResDTO.ProductListResponse getHomeProducts(Long userId) {
        Pageable pageable = PageRequest.of(0, 6);
        List<Product> products = fittingRepository.findTop6ProductsByUserIdOrderByUpdatedAtDesc(userId, pageable);
        
        List<HomeResDTO.ProductDTO> productList = products.stream()
                .map(HomeConverter::toProductDTO)
                .collect(Collectors.toList());

        return HomeResDTO.ProductListResponse.builder()
                .products(productList)
                .build();
    }
}
