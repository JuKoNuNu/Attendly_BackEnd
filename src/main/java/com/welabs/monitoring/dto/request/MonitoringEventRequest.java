package com.welabs.monitoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MonitoringEventRequest(
        @NotBlank String studentId,
        String studentName,
        String cohort,
        String assetCode,
        String sessionId,
        @NotBlank String eventType,
        @NotNull Long timestamp,
        Integer duration,
        String previousState,
        String currentState,

        // v6: 위장 의심 이벤트 (eventType="spoof_suspected") 전용
        Integer score,
        List<String> reasons,
        Integer samples,
        Integer blinks
) {}
