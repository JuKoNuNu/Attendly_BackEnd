package com.welabs.monitoring.controller;

import com.welabs.monitoring.dto.request.StudentBatchRequest;
import com.welabs.monitoring.dto.response.BatchResultResponse;
import com.welabs.monitoring.dto.response.StudentExistsResponse;
import com.welabs.monitoring.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class StudentAdminController {

    private final StudentService service;

    @GetMapping("/{studentId}/exists")
    public ResponseEntity<StudentExistsResponse> exists(@PathVariable String studentId) {
        return ResponseEntity.ok(service.checkExists(studentId));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchResultResponse> batch(@RequestBody StudentBatchRequest req) {
        return ResponseEntity.ok(service.registerBatch(req.students()));
    }
}
