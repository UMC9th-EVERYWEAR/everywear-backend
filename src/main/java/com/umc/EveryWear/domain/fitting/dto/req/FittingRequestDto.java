package com.umc.EveryWear.domain.fitting.dto.req;

public class FittingRequestDto {

    public record FittingRequest(
            Long productId,
            String userImageUrl,
            String garmentImageUrl
    ) {}
}
