package com.welabs.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "v6_student_heartbeat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentHeartbeat {

    @Id
    @Column(name = "student_id", length = 128)
    private String studentId;

    @Column(name = "last_at", nullable = false)
    private LocalDateTime lastAt;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
