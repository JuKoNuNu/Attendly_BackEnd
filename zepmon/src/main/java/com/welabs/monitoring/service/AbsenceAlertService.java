package com.welabs.monitoring.service;

import com.welabs.monitoring.entity.AbsenceSession;
import com.welabs.monitoring.entity.StudentMaster;
import com.welabs.monitoring.repository.AbsenceSessionRepository;
import com.welabs.monitoring.repository.StudentMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenceAlertService {

    private final AbsenceSessionRepository absenceRepo;
    private final StudentMasterRepository studentRepo;
    private final TeamsNotifier teamsNotifier;

    @Value("${notification.threshold-sec:300}")
    private int thresholdSec;

    @Scheduled(fixedRate = 10_000)
    @Transactional
    public void dispatchPendingAlerts() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(thresholdSec);
        var pending = absenceRepo.findPendingAlerts(threshold);

        if (pending.isEmpty()) return;
        log.info("Pending alerts: {}", pending.size());

        for (AbsenceSession session : pending) {
            StudentMaster student = studentRepo.findById(session.getStudentId()).orElse(null);
            if (student == null) {
                log.warn("Unknown student in pending alert: {}", session.getStudentId());
                continue;
            }

            long elapsed = Duration.between(session.getStartedAt(), LocalDateTime.now()).toSeconds();
            boolean sent = teamsNotifier.notifyAbsenceLong(student, elapsed);

            if (sent) {
                session.setIsLong(true);
                session.setAlertSentAt(LocalDateTime.now());
                log.info("Alert sent: {} ({}s)", student.getStudentId(), elapsed);
            }
        }
    }
}
