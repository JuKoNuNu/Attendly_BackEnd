package com.welabs.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "v6_absence_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", length = 128, nullable = false)
    private String studentId;

    @Column(name = "session_id", length = 64, nullable = false)
    private String sessionId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "is_long")
    private Boolean isLong;

    @Column(name = "alert_sent_at")
    private LocalDateTime alertSentAt;
}
