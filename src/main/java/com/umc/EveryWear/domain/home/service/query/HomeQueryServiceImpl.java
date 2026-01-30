package com.umc.EveryWear.domain.home.service.query;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.entity.FittingHistory;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
import com.umc.EveryWear.domain.home.converter.HomeConverter;
import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
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

    private final UserProductRepository userProductRepository;
    private final FittingHistoryRepository fittingHistoryRepository;

    @Override
    public HomeResDTO.ProductListResponse getHomeProducts(Long userId) {
        Pageable pageable = PageRequest.of(0, 6);
        List<Product> products = userProductRepository.findTop6ProductsByUserIdOrderByUpdatedAtDesc(userId, pageable);
        
        List<HomeResDTO.ProductDTO> productList = products.stream()
                .map(HomeConverter::toProductDTO)
                .collect(Collectors.toList());

        return HomeResDTO.ProductListResponse.builder()
                .products(productList)
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
