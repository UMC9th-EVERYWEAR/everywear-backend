package com.umc.EveryWear.domain.closet.service.query;

import com.umc.EveryWear.domain.closet.converter.ClosetConverter;
import com.umc.EveryWear.domain.closet.dto.res.ClosetResDTO;
import com.umc.EveryWear.domain.closet.exception.ClosetException;
import com.umc.EveryWear.domain.closet.exception.code.ClosetErrorCode;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
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
    private final FittingHistoryRepository fittingHistoryRepository;

    @Override
    public ClosetResDTO.ProductListResponse getClosetProducts(Long userId) {
        validateUserId(userId);
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdOrderByUpdatedAtDesc(userId);

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(userProduct -> ClosetConverter.toProductDTO(
                        userProduct,
                        getRecentFittingId(userId, userProduct)
                ))
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public ClosetResDTO.ProductListResponse getClosetTopProducts(Long userId) {
        validateUserId(userId);
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, "상의");

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(userProduct -> ClosetConverter.toProductDTO(
                        userProduct,
                        getRecentFittingId(userId, userProduct)
                ))
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public ClosetResDTO.ProductListResponse getClosetBottomProducts(Long userId) {
        validateUserId(userId);
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, "하의");

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(userProduct -> ClosetConverter.toProductDTO(
                        userProduct,
                        getRecentFittingId(userId, userProduct)
                ))
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public ClosetResDTO.ProductListResponse getClosetOuterProducts(Long userId) {
        validateUserId(userId);
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, "아우터");

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(userProduct -> ClosetConverter.toProductDTO(
                        userProduct,
                        getRecentFittingId(userId, userProduct)
                ))
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public ClosetResDTO.ProductListResponse getClosetDressProducts(Long userId) {
        validateUserId(userId);
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, "원피스");

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(userProduct -> ClosetConverter.toProductDTO(
                        userProduct,
                        getRecentFittingId(userId, userProduct)
                ))
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public ClosetResDTO.ProductListResponse getClosetEtcProducts(Long userId) {
        validateUserId(userId);
        List<UserProduct> userProducts = userProductRepository.findLikedProductsByUserIdAndCategoryOrderByUpdatedAtDesc(userId, "기타");

        List<ClosetResDTO.ProductDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(userProduct -> ClosetConverter.toProductDTO(
                        userProduct,
                        getRecentFittingId(userId, userProduct)
                ))
                .collect(Collectors.toList());

        return ClosetResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new ClosetException(ClosetErrorCode.CLOSET_UNAUTHORIZED);
        }
    }

    // 사용자 상품에 대한 가장 최근 피팅 내역의 ID를 조회한다.
    // 피팅 내역이 없으면 null을 반환한다.
    private Long getRecentFittingId(Long userId, UserProduct userProduct) {
        Long productId = userProduct.getProduct().getProductId();

        return fittingHistoryRepository
                .findTop1ByUserProduct_User_UserIdAndUserProduct_Product_ProductIdOrderByUpdatedAtDesc(userId, productId)
                .map(FittingHistory::getFittingId)
                .orElse(null);
    }
}
