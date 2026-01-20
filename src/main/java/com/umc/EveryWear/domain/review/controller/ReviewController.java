package com.umc.EveryWear.domain.review.controller;

import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    // 조회 요청해서 있으면 반환 없으면 없다 반환 -> 최초 1회
    // 조회 요청 API
    @Operation(summary = "리뷰 등록", description = "리뷰를 등록합니다.")
    @PostMapping("/import/musinsa")
    public ApiResponse<> Review() {}

    // 생성 요청 API

    // 주기 조회 API
}
