package com.umc.EveryWear.domain.home.service.query;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
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
    private final FittingHistoryRepository fittingHistoryRepository;

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

    @Override
    public List<FittingResponseDto.FittingSummary> getRecentFittings(Long userId) {

        return fittingHistoryRepository
                .findTop6ByUserProduct_User_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private FittingResponseDto.FittingSummary toSummary(FittingHistory h) {
        return new FittingResponseDto.FittingSummary(
                h.getFittingId(),
                h.getFittingResultImage(),
                h.getUserProduct().getIsLiked(),
                h.getCreatedAt()
        );
    }
}
