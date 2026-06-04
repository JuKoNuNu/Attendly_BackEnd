package com.welabs.monitoring.repository;

import com.welabs.monitoring.entity.StudentHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StudentHeartbeatRepository extends JpaRepository<StudentHeartbeat, String> {

    @Modifying
    @Query(value = """
        INSERT INTO v6_student_heartbeat (student_id, last_at, session_id)
        VALUES (:studentId, :lastAt, :sessionId)
        ON DUPLICATE KEY UPDATE
          last_at = VALUES(last_at),
          session_id = VALUES(session_id)
    """, nativeQuery = true)
    void upsert(@Param("studentId") String studentId,
                @Param("lastAt") LocalDateTime lastAt,
                @Param("sessionId") String sessionId);
}
