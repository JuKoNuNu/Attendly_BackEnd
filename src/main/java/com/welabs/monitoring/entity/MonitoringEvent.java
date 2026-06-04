package com.welabs.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "v6_monitoring_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", length = 128, nullable = false)
    private String studentId;

    @Column(name = "session_id", length = 64, nullable = false)
    private String sessionId;

    @Column(name = "event_type", length = 32, nullable = false)
    private String eventType;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "previous_state", length = 16)
    private String previousState;

    @Column(name = "current_state", length = 16)
    private String currentState;

    @Column(name = "raw_payload", columnDefinition = "JSON")
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
