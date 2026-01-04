package com.umc.EveryWear.global.controller;

import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "서버 상태 확인 API")
public class HealthCheckController {

    @GetMapping("/")
    @Operation(summary = "기본 헬스체크", description = "ALB 타겟 그룹 헬스체크용 엔드포인트")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health")
    @Operation(summary = "상세 헬스체크", description = "서버 상태 상세 정보 제공")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detailedHealthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "EveryWear Backend");

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.OK, healthInfo)
        );
    }
}