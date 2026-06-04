package com.welabs.monitoring.repository;

import com.welabs.monitoring.entity.AbsenceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AbsenceSessionRepository extends JpaRepository<AbsenceSession, Long> {

    Optional<AbsenceSession> findFirstByStudentIdAndEndedAtIsNullOrderByStartedAtDesc(String studentId);

    List<AbsenceSession> findByStudentIdOrderByStartedAtDesc(String studentId);

    @Query("""
        SELECT a FROM AbsenceSession a
        WHERE a.endedAt IS NULL
          AND a.alertSentAt IS NULL
          AND a.startedAt <= :threshold
    """)
    List<AbsenceSession> findPendingAlerts(@Param("threshold") LocalDateTime threshold);
}
