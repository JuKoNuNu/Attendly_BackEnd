package com.welabs.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_master")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentMaster {

    @Id
    @Column(name = "student_id", length = 128)
    private String studentId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "cohort", length = 64, nullable = false)
    private String cohort;

    @Column(name = "asset_code", length = 32)
    private String assetCode;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDate enrolledAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
