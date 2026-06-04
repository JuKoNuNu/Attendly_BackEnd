package com.welabs.monitoring.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record LiveSessionsResponse(List<StudentLiveStatus> students) {
    public record StudentLiveStatus(
            String studentId,
            String name,
            String cohort,
            String assetCode,
            String currentState,
            String sessionId,
            LocalDateTime lastHeartbeat,
            LocalDateTime absenceStartedAt,
            Integer absenceDurationSec,

            // v6: 위장 의심 (최근 5분 내 최고 점수)
            Integer spoofScore,
            LocalDateTime spoofLastAt
    ) {}
}
