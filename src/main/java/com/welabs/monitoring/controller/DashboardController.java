package com.welabs.monitoring.controller;

import com.welabs.monitoring.dto.response.LiveSessionsResponse;
import com.welabs.monitoring.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/sessions/live")
    public ResponseEntity<LiveSessionsResponse> live(
            @RequestParam(required = false) String cohort) {
        return ResponseEntity.ok(service.getLiveSessions(cohort));
    }
}
