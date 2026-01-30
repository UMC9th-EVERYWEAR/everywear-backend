package com.umc.EveryWear.domain.review.service.command;

import com.umc.EveryWear.domain.review.dto.req.ReviewReqDTO;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;

public interface ReviewCommandService {
    ReviewResDTO.CrawlResponseDTO startReviewCrawling(ReviewReqDTO.CrawlReviewDTO dto);
}