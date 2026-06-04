package com.welabs.monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.welabs.monitoring.dto.request.MonitoringEventRequest;
import com.welabs.monitoring.entity.AbsenceSession;
import com.welabs.monitoring.entity.MonitoringEvent;
import com.welabs.monitoring.entity.SpoofEvent;
import com.welabs.monitoring.entity.StudentMaster;
import com.welabs.monitoring.repository.AbsenceSessionRepository;
import com.welabs.monitoring.repository.MonitoringEventRepository;
import com.welabs.monitoring.repository.SpoofEventRepository;
import com.welabs.monitoring.repository.StudentHeartbeatRepository;
import com.welabs.monitoring.repository.StudentMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private static final int SPOOF_PERSIST_MIN_SCORE = 50;

    private final StudentMasterRepository studentRepo;
    private final MonitoringEventRepository eventRepo;
    private final AbsenceSessionRepository absenceRepo;
    private final SpoofEventRepository spoofRepo;
    private final StudentHeartbeatRepository heartbeatRepo;
    private final ObjectMapper objectMapper;
    private final TeamsNotifier teamsNotifier;

    @Value("${notification.spoof-threshold:70}")
    private int spoofThreshold;

    @Transactional
    public void handleEvent(MonitoringEventRequest req) {
        if (!studentRepo.existsById(req.studentId())) {
            log.warn("Unknown studentId: {}", req.studentId());
            throw new IllegalArgumentException("Unknown studentId: " + req.studentId());
        }

        LocalDateTime occurredAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(req.timestamp()), ZoneId.systemDefault());

        String sessionId = req.sessionId() != null ? req.sessionId() : "unknown";

        // v6: heartbeat 은 monitoring_event 에 저장 X, heartbeat 테이블에 UPSERT 만
        if ("heartbeat".equals(req.eventType())) {
            heartbeatRepo.upsert(req.studentId(), occurredAt, sessionId);
            return;
        }

        // v6: spoof_suspected 50점 미만 노이즈는 저장 안 함
        if ("spoof_suspected".equals(req.eventType())) {
            int score = req.score() != null ? req.score() : 0;
            if (score < SPOOF_PERSIST_MIN_SCORE) {
                log.debug("Low spoof score, skip persist: studentId={} score={}", req.studentId(), score);
                return;
            }
        }

        MonitoringEvent event = MonitoringEvent.builder()
                .studentId(req.studentId())
                .sessionId(sessionId)
                .eventType(req.eventType())
                .occurredAt(occurredAt)
                .durationSec(req.duration())
                .previousState(req.previousState())
                .currentState(req.currentState())
                .rawPayload(toJson(req))
                .build();
        eventRepo.save(event);

        switch (req.eventType()) {
            case "session_started" -> { /* DB 기록만 */ }
            case "state_changed" -> handleStateChanged(req, occurredAt);
            case "absence_long" -> handleAbsenceLong(req, occurredAt);
            case "presence_recovered" -> handlePresenceRecovered(req, occurredAt);
            case "session_ended" -> { /* DB 기록만 */ }
            case "spoof_suspected" -> handleSpoofSuspected(req, occurredAt);
            default -> log.debug("Unhandled eventType: {}", req.eventType());
        }

        log.info("Event handled: {} - {} - sessionId={}",
                req.studentId(), req.eventType(), sessionId);
    }

    private void handleStateChanged(MonitoringEventRequest req, LocalDateTime occurredAt) {
        if ("AWAY_BRIEF".equals(req.currentState())) {
            AbsenceSession session = AbsenceSession.builder()
                    .studentId(req.studentId())
                    .sessionId(req.sessionId() != null ? req.sessionId() : "unknown")
                    .startedAt(occurredAt)
                    .isLong(false)
                    .build();
            absenceRepo.save(session);
        }
    }

    private void handleAbsenceLong(MonitoringEventRequest req, LocalDateTime occurredAt) {
        // 클라이언트가 5분 임계 도달 인식. is_long 만 마킹.
        // 실제 외부 알림(Teams 등)은 AbsenceAlertService 가 별도 처리.
        absenceRepo.findFirstByStudentIdAndEndedAtIsNullOrderByStartedAtDesc(req.studentId())
                .ifPresent(s -> s.setIsLong(true));
    }

    private void handlePresenceRecovered(MonitoringEventRequest req, LocalDateTime occurredAt) {
        absenceRepo.findFirstByStudentIdAndEndedAtIsNullOrderByStartedAtDesc(req.studentId())
                .ifPresent(s -> {
                    s.setEndedAt(occurredAt);
                    s.setDurationSec(req.duration());
                    absenceRepo.save(s);
                });
    }

    private void handleSpoofSuspected(MonitoringEventRequest req, LocalDateTime occurredAt) {
        int score = req.score() != null ? req.score() : 0;

        SpoofEvent e = SpoofEvent.builder()
                .studentId(req.studentId())
                .sessionId(req.sessionId())
                .score(score)
                .samples(req.samples())
                .blinks(req.blinks())
                .occurredAt(occurredAt)
                .build();

        try {
            e.setReasons(objectMapper.writeValueAsString(req.reasons()));
        } catch (JsonProcessingException ex) {
            log.warn("reasons 직렬화 실패: {}", ex.getMessage());
            e.setReasons("[]");
        }

        spoofRepo.save(e);

        if (score >= spoofThreshold) {
            StudentMaster student = studentRepo.findById(req.studentId()).orElse(null);
            if (student != null) {
                teamsNotifier.notifySpoofSuspected(student, score, req.reasons());
            }
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
