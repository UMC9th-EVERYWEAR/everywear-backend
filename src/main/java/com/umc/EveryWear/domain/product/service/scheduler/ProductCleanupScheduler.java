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

// 60일 경과된 UserProduct를 자동으로 삭제하는 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCleanupScheduler {

    private final UserProductRepository userProductRepository;
    
    private static final int EXPIRATION_DAYS = 60;

    // 매일 자정(00:00:00)에 실행되어 60일 경과된 UserProduct를 삭제
    // cron 표현식: 초 분 시 일 월 요일
    // "0 10 16 * * ?" = 매일 16:10:00에 실행
    // "0 * * * * ?" = 테스트용(1분마다 실행)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredUserProducts() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(EXPIRATION_DAYS);
            log.info("60일 경과된 UserProduct 삭제 작업 시작. 기준 날짜: {}", cutoffDate);
            
            List<UserProduct> expiredProducts = userProductRepository.findExpiredUserProducts(cutoffDate);
            
            if (expiredProducts.isEmpty()) {
                log.info("삭제할 만료된 UserProduct가 없습니다.");
                return;
            }
            
            int deletedCount = expiredProducts.size();
            userProductRepository.deleteAll(expiredProducts);
            
            log.info("60일 경과된 UserProduct {}개 삭제 완료", deletedCount);
            
        } catch (Exception e) {
            log.error("60일 경과된 UserProduct 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
