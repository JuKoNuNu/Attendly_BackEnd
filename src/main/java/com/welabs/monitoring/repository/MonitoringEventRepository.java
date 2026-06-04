package com.welabs.monitoring.repository;

import com.welabs.monitoring.entity.MonitoringEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MonitoringEventRepository extends JpaRepository<MonitoringEvent, Long> {
    List<MonitoringEvent> findByStudentIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            String studentId, LocalDateTime from, LocalDateTime to);
    List<MonitoringEvent> findBySessionIdOrderByOccurredAtAsc(String sessionId);

    @Modifying
    @Query(value = """
        DELETE FROM v6_monitoring_event
        WHERE occurred_at < :cutoff
        LIMIT :batch
    """, nativeQuery = true)
    int deleteOldBatch(@Param("cutoff") LocalDateTime cutoff,
                       @Param("batch") int batch);
}
