package com.welabs.monitoring.controller;

import com.welabs.monitoring.dto.request.MonitoringEventRequest;
import com.welabs.monitoring.service.MonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService service;

    @PostMapping("/events")
    public ResponseEntity<Map<String, Boolean>> receiveEvent(
            @Valid @RequestBody MonitoringEventRequest req) {
        service.handleEvent(req);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
