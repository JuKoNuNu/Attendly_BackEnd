package com.welabs.monitoring.dto.request;

import java.util.List;

public record StudentBatchRequest(List<StudentItem> students) {
    public record StudentItem(
            String studentId,
            String name,
            String cohort,
            String assetCode
    ) {}
}
