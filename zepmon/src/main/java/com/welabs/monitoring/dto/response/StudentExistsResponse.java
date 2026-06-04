package com.welabs.monitoring.dto.response;

public record StudentExistsResponse(
        boolean exists,
        String name,
        String cohort
) {}
