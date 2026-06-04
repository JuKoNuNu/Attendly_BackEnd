package com.welabs.monitoring.service;

import com.welabs.monitoring.dto.response.LiveSessionsResponse;
import com.welabs.monitoring.entity.AbsenceSession;
import com.welabs.monitoring.entity.StudentHeartbeat;
import com.welabs.monitoring.entity.StudentMaster;
import com.welabs.monitoring.repository.AbsenceSessionRepository;
import com.welabs.monitoring.repository.SpoofEventRepository;
import com.welabs.monitoring.repository.SpoofEventRepository.SpoofSummaryProjection;
import com.welabs.monitoring.repository.StudentHeartbeatRepository;
import com.welabs.monitoring.repository.StudentMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final long ALIVE_WINDOW_SEC = 60;
    private static final long SPOOF_LOOKBACK_MIN = 5;
    private static final long ABSENCE_LONG_SEC = 300;

    private final StudentMasterRepository studentRepo;
    private final AbsenceSessionRepository absenceRepo;
    private final StudentHeartbeatRepository heartbeatRepo;
    private final SpoofEventRepository spoofRepo;

    @Transactional(readOnly = true)
    public LiveSessionsResponse getLiveSessions(String cohort) {
        List<StudentMaster> students = (cohort != null && !cohort.isBlank())
                ? studentRepo.findByCohortAndDeletedAtIsNull(cohort)
                : studentRepo.findAllByDeletedAtIsNull();

        if (students.isEmpty()) {
            return new LiveSessionsResponse(List.of());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime spoofSince = now.minusMinutes(SPOOF_LOOKBACK_MIN);

        List<String> studentIds = students.stream().map(StudentMaster::getStudentId).toList();

        // N+1 회피: heartbeat 한 번에 조회
        Map<String, StudentHeartbeat> heartbeatMap = heartbeatRepo.findAllById(studentIds).stream()
                .collect(Collectors.toMap(StudentHeartbeat::getStudentId, Function.identity()));

        // N+1 회피: 위장 정보 한 번에 조회
        Map<String, SpoofSummaryProjection> spoofMap = spoofRepo.findRecentSummary(spoofSince).stream()
                .collect(Collectors.toMap(SpoofSummaryProjection::getStudentId, Function.identity()));

        List<LiveSessionsResponse.StudentLiveStatus> statuses = students.stream()
                .map(s -> buildStatus(s, now, heartbeatMap.get(s.getStudentId()), spoofMap.get(s.getStudentId())))
                .toList();

        return new LiveSessionsResponse(statuses);
    }

    private LiveSessionsResponse.StudentLiveStatus buildStatus(
            StudentMaster s,
            LocalDateTime now,
            StudentHeartbeat hb,
            SpoofSummaryProjection spoof) {

        // 1) heartbeat 테이블 기반 alive 판정
        boolean alive = hb != null
                && Duration.between(hb.getLastAt(), now).toSeconds() < ALIVE_WINDOW_SEC;

        // 2) 진행 중 자리비움 세션
        AbsenceSession ongoing = absenceRepo
                .findFirstByStudentIdAndEndedAtIsNullOrderByStartedAtDesc(s.getStudentId())
                .orElse(null);

        // 3) 상태 판정 우선순위
        //    a. heartbeat 60초 이상 미수신 → OFFLINE
        //    b. 진행 중 absence_session 있음 → AWAY_LONG / AWAY_BRIEF
        //    c. 그 외                       → PRESENT
        String currentState;
        String sessionId = null;
        LocalDateTime lastHeartbeat = null;

        if (!alive) {
            currentState = "OFFLINE";
        } else {
            lastHeartbeat = hb.getLastAt();
            sessionId = hb.getSessionId();

            if (ongoing != null) {
                long elapsedSec = Duration.between(ongoing.getStartedAt(), now).toSeconds();
                boolean isLong = Boolean.TRUE.equals(ongoing.getIsLong()) || elapsedSec >= ABSENCE_LONG_SEC;
                currentState = isLong ? "AWAY_LONG" : "AWAY_BRIEF";
            } else {
                currentState = "PRESENT";
            }
        }

        // 4) 자리비움 진행시간
        LocalDateTime absenceStartedAt = null;
        Integer absenceDurationSec = null;
        if (ongoing != null) {
            absenceStartedAt = ongoing.getStartedAt();
            absenceDurationSec = (int) Duration.between(absenceStartedAt, now).toSeconds();
        }

        // 5) 위장 정보
        Integer spoofScore = spoof != null ? spoof.getMaxScore() : null;
        LocalDateTime spoofLastAt = spoof != null ? spoof.getLastAt() : null;

        return new LiveSessionsResponse.StudentLiveStatus(
                s.getStudentId(),
                s.getName(),
                s.getCohort(),
                s.getAssetCode(),
                currentState,
                sessionId,
                lastHeartbeat,
                absenceStartedAt,
                absenceDurationSec,
                spoofScore,
                spoofLastAt
        );
    }
}
