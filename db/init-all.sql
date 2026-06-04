-- ============================================================
-- welabs_monitoring 전체 스키마 (DBeaver / Workbench 에서 한 번에 실행)
--   DBeaver: 이 파일 열고  Alt+X (Execute SQL Script)
--   네이티브 MySQL 새로 깐 곳에서 그대로 실행하면 DB+테이블+테스트데이터 생성
-- 모두 IF NOT EXISTS / INSERT IGNORE 라 여러 번 실행해도 안전(멱등).
-- ============================================================
CREATE DATABASE IF NOT EXISTS welabs_monitoring
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE welabs_monitoring;

-- 1. 학생 마스터
CREATE TABLE IF NOT EXISTS student_master (
    student_id VARCHAR(128) NOT NULL COMMENT '예: LGCNS 5기 - 이주형 A',
    name VARCHAR(64) NOT NULL,
    cohort VARCHAR(64) NOT NULL COMMENT '기수',
    asset_code VARCHAR(32) DEFAULT NULL COMMENT '노트북 자산번호',
    enrolled_at DATE NOT NULL DEFAULT (CURRENT_DATE),
    deleted_at DATETIME DEFAULT NULL,
    PRIMARY KEY (student_id),
    INDEX idx_cohort (cohort),
    INDEX idx_asset (asset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. 모니터링 이벤트 (v5)
CREATE TABLE IF NOT EXISTS monitoring_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(128) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL COMMENT 'session_started/heartbeat/absence_long 등',
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
    CONSTRAINT fk_event_student FOREIGN KEY (student_id)
        REFERENCES student_master(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 3. 자리비움 세션 (v5)
CREATE TABLE IF NOT EXISTS absence_session (
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
    CONSTRAINT fk_absence_student FOREIGN KEY (student_id)
        REFERENCES student_master(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 4. v6 모니터링 이벤트
CREATE TABLE IF NOT EXISTS v6_monitoring_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(128) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL COMMENT 'session_started/state_changed/absence_long 등',
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
    INDEX idx_occurred_at (occurred_at),
    CONSTRAINT fk_v6_event_student FOREIGN KEY (student_id)
        REFERENCES student_master(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 5. v6 자리비움 세션
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

-- 6. v6 위장 의심 이벤트
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

-- 7. v6 학생 heartbeat (학생당 1 row UPSERT)
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

-- 8. 테스트 데이터
INSERT IGNORE INTO student_master (student_id, name, cohort, asset_code) VALUES
('LGCNS 5기 - 이주형 A', '이주형', 'LGCNS 5기', 'NB-001'),
('LGCNS 5기 - 이주형 B', '이주형', 'LGCNS 5기', 'NB-002'),
('LGCNS 5기 - 김철수', '김철수', 'LGCNS 5기', 'NB-003'),
('LGCNS 5기 - 박민수', '박민수', 'LGCNS 5기', 'NB-004'),
('LGCNS 5기 - 최지원', '최지원', 'LGCNS 5기', 'NB-005');

-- 9. 적용 확인
SHOW TABLES;
SELECT student_id, name, cohort, asset_code FROM student_master;
