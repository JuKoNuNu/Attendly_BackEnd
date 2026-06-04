package com.welabs.monitoring.service;

import com.welabs.monitoring.dto.request.StudentBatchRequest;
import com.welabs.monitoring.dto.response.BatchResultResponse;
import com.welabs.monitoring.dto.response.StudentExistsResponse;
import com.welabs.monitoring.entity.StudentMaster;
import com.welabs.monitoring.repository.StudentMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMasterRepository repo;

    public StudentExistsResponse checkExists(String studentId) {
        return repo.findByStudentIdAndDeletedAtIsNull(studentId)
                .map(s -> new StudentExistsResponse(true, s.getName(), s.getCohort()))
                .orElse(new StudentExistsResponse(false, null, null));
    }

    @Transactional
    public BatchResultResponse registerBatch(List<StudentBatchRequest.StudentItem> items) {
        int inserted = 0;
        int skipped = 0;

        for (StudentBatchRequest.StudentItem item : items) {
            if (repo.existsById(item.studentId())) {
                skipped++;
                continue;
            }
            repo.save(StudentMaster.builder()
                    .studentId(item.studentId())
                    .name(item.name())
                    .cohort(item.cohort())
                    .assetCode(item.assetCode())
                    .enrolledAt(LocalDate.now())
                    .build());
            inserted++;
        }

        return new BatchResultResponse(inserted, skipped);
    }
}
