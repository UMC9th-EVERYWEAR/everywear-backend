package com.umc.EveryWear.domain.product.service.query;

import com.umc.EveryWear.domain.product.converter.ProductConverter;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.exception.ProductException;
import com.umc.EveryWear.domain.product.exception.code.ProductErrorCode;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import com.umc.EveryWear.domain.user.service.query.UserImgQueryService;
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
    private final UserImgQueryService userImgQueryService;

    @Override
    public ProductResDTO.ProductListResponse getAllProducts(Long userId) {
        List<UserProduct> userProducts = userProductRepository.findAllByUserIdOrderByUpdatedAtDesc(userId);
        return toProductListResponse(userProducts);
    }

    @Override
    public ProductResDTO.ProductListResponse getProductsByCategory(Long userId, String category) {
        List<UserProduct> userProducts = userProductRepository.findAllByUserIdAndCategoryOrderByUpdatedAtDesc(userId, category);
        return toProductListResponse(userProducts);
    }

    @Override
    public ProductResDTO.ProductForFittingResponse getProductForFitting(Long userId, Long productId) {
        UserProduct userProduct = userProductRepository.findByUser_UserIdAndProduct_ProductId(userId, productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
        var representativeImg = userImgQueryService.getRepresentativeImage(userId);
        ProductResDTO.ProductForFittingDTO productDto = ProductConverter.toProductForFittingDTO(userProduct);
        ProductResDTO.RepresentativeImgDTO representativeImgDto = ProductResDTO.RepresentativeImgDTO.builder()
                .profileImageId(representativeImg.getProfileImageId())
                .imageUrl(representativeImg.getImageUrl())
                .representative(representativeImg.isRepresentative())
                .build();
        return ProductResDTO.ProductForFittingResponse.builder()
                .product(productDto)
                .representative_img(representativeImgDto)
                .build();
    }

    private static ProductResDTO.ProductListResponse toProductListResponse(List<UserProduct> userProducts) {
        List<ProductResDTO.ListDTO> products = userProducts.isEmpty()
                ? null
                : userProducts.stream()
                .map(ProductConverter::toListDTO)
                .collect(Collectors.toList());
        return ProductResDTO.ProductListResponse.builder()
                .products(products)
                .build();
    }
}
