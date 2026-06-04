package com.welabs.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "v6_spoof_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpoofEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", length = 128, nullable = false)
    private String studentId;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String reasons;

    private Integer samples;

    private Integer blinks;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
