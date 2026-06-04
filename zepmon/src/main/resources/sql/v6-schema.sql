-- v6 전용 테이블. v5 의 student_master 는 공유, 그 외는 v6_ prefix 로 격리.
-- DB 는 welabs_monitoring 공유. 실행 전 v5 schema.sql 이 적용되어 있어야 함 (student_master 존재).

USE welabs_monitoring;

-- 1. v6 모니터링 이벤트 (v5 schema 와 동일)
CREATE TABLE IF NOT EXISTS v6_monitoring_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(128) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL COMMENT 'session_started/state_changed/absence_long 등 (heartbeat 은 별도 테이블)',
    occurred_at DATETIME(3) NOT NULL,
    duration_sec INT DEFAULT NULL,
    previous_state VARCHAR(16) DEFAULT NULL,
    current_state VARCHAR(16) DEFAULT NULL,
    raw_payload JSON DEFAULT NULL,
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_student_time (student_id, occurred_at),
    INDEX idx_session (session_id),
    INDEX idx_event_type (event_type),
    INDEX idx_occurred_at (occurred_at),                       -- TTL 삭제용
    CONSTRAINT fk_v6_event_student FOREIGN KEY (student_id)
        REFERENCES student_master(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. v6 자리비움 세션 (v5 schema 와 동일)
CREATE TABLE IF NOT EXISTS v6_absence_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(128) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    started_at DATETIME(3) NOT NULL,
    ended_at DATETIME(3) DEFAULT NULL,
    duration_sec INT DEFAULT NULL,
    is_long BOOLEAN DEFAULT FALSE COMMENT '5분 임계 도달 여부',
    alert_sent_at DATETIME(3) DEFAULT NULL,
    PRIMARY KEY (id),
    INDEX idx_student_time (student_id, started_at),
    INDEX idx_student_ended (student_id, ended_at, started_at DESC),
    CONSTRAINT fk_v6_absence_student FOREIGN KEY (student_id)
        REFERENCES student_master(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 3. 위장 의심 이벤트 (신규)
CREATE TABLE IF NOT EXISTS v6_spoof_event (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    student_id   VARCHAR(128) NOT NULL,
    session_id   VARCHAR(64),
    score        INT          NOT NULL,
    reasons      TEXT         COMMENT 'JSON array of strings',
    samples      INT,
    blinks       INT,
    occurred_at  DATETIME(3)  NOT NULL,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    INDEX idx_student_occurred (student_id, occurred_at DESC),
    INDEX idx_score_occurred (score, occurred_at DESC),
    CONSTRAINT fk_v6_spoof_student FOREIGN KEY (student_id)
        REFERENCES student_master(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 4. 학생 heartbeat (신규, 학생당 1 row UPSERT)
CREATE TABLE IF NOT EXISTS v6_student_heartbeat (
    student_id    VARCHAR(128) NOT NULL,
    last_at       DATETIME(3)  NOT NULL,
    session_id    VARCHAR(64),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (student_id),
    INDEX idx_last_at (last_at),
    CONSTRAINT fk_v6_hb_student FOREIGN KEY (student_id)
        REFERENCES student_master(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
