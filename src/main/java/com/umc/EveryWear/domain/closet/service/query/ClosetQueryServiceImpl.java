package com.umc.EveryWear.domain.closet.service.query;

import com.umc.EveryWear.domain.closet.converter.ClosetConverter;
import com.umc.EveryWear.domain.closet.dto.res.ClosetResDTO;
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
public class ClosetQueryServiceImpl implements ClosetQueryService {

    private final UserProductRepository userProductRepository;

    @Override
    public ClosetResDTO.ProductListResponse getClosetProducts(Long userId) {
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdOrderByUpdatedAtDesc(userId);

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(ClosetConverter::toProductDTO)
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public ClosetResDTO.ProductListResponse getClosetTopProducts(Long userId) {
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, "상의");

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(ClosetConverter::toProductDTO)
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public ClosetResDTO.ProductListResponse getClosetBottomProducts(Long userId) {
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, "하의");

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(ClosetConverter::toProductDTO)
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }
}
