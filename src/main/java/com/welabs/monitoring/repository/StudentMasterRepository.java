package com.welabs.monitoring.repository;

import com.welabs.monitoring.entity.StudentMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentMasterRepository extends JpaRepository<StudentMaster, String> {
    Optional<StudentMaster> findByStudentIdAndDeletedAtIsNull(String studentId);
    List<StudentMaster> findByCohortAndDeletedAtIsNull(String cohort);
    List<StudentMaster> findAllByDeletedAtIsNull();
}
