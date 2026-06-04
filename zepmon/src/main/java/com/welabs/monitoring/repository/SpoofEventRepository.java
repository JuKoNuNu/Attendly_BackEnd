package com.welabs.monitoring.repository;

import com.welabs.monitoring.entity.SpoofEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpoofEventRepository extends JpaRepository<SpoofEvent, Long> {

    List<SpoofEvent> findByStudentIdOrderByOccurredAtDesc(String studentId);

    @Query("""
        SELECT s.studentId AS studentId, MAX(s.score) AS maxScore, MAX(s.occurredAt) AS lastAt
        FROM SpoofEvent s
        WHERE s.occurredAt >= :since
        GROUP BY s.studentId
    """)
    List<SpoofSummaryProjection> findRecentSummary(@Param("since") LocalDateTime since);

    interface SpoofSummaryProjection {
        String getStudentId();
        Integer getMaxScore();
        LocalDateTime getLastAt();
    }
}
