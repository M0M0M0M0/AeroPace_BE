package com.group1.aeropace.scheduled;

import com.group1.aeropace.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewScheduler {

    private final ReviewRepository reviewRepository;

    // 2am
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expirePendingReviews() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int count = reviewRepository.expireOldPendingReviews(cutoff);
        log.info("[ReviewScheduler] Expired {} pending reviews older than 30 days", count);
    }
}