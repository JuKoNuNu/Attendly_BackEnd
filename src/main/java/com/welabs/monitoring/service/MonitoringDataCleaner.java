package com.welabs.monitoring.service;

import com.welabs.monitoring.repository.MonitoringEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringDataCleaner {

    private static final int BATCH_SIZE = 10_000;

    private final MonitoringEventRepository eventRepo;

    @Value("${monitoring.retention-days:30}")
    private int retentionDays;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        int total = 0;
        while (true) {
            int n = eventRepo.deleteOldBatch(cutoff, BATCH_SIZE);
            if (n == 0) break;
            total += n;
        }
        log.info("Old events cleaned: cutoff={} deleted={} rows", cutoff, total);
    }
}
