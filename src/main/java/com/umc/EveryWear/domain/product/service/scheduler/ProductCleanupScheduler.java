package com.umc.EveryWear.domain.product.service.scheduler;

import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// 60일 경과되고 내 옷장에 넣지 않은 UserProduct를 삭제.
// 삭제 시 FittingHistory는 cascade로 함께 삭제됨.
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCleanupScheduler {

    private final UserProductRepository userProductRepository;
    private static final int EXPIRATION_DAYS = 60;

    // 매일 자정(00:00:00)에 실행되어 60일 경과되고 is_liked가 false인 UserProduct를 삭제
    // UserProduct 삭제 시 해당 상품의 피팅 내역(fitting_history)은 cascade로 함께 삭제됨
    // cron 표현식: 초 분 시 일 월 요일
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredUserProducts() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(EXPIRATION_DAYS);
            log.info("UserProduct 삭제 작업 시작. 기준 날짜: {}", cutoffDate);

            List<UserProduct> expiredProducts = userProductRepository.findAllByDelete(cutoffDate);

            if (expiredProducts.isEmpty()) {
                log.info("삭제할 만료된 UserProduct가 없습니다.");
                return;
            }

            int deletedCount = expiredProducts.size();
            userProductRepository.deleteAll(expiredProducts);

            log.info("60일 경과되고 좋아요하지 않은 UserProduct {}개 삭제 완료 (연관 피팅내역 포함)", deletedCount);

        } catch (Exception e) {
            log.error("60일 경과된 UserProduct 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
