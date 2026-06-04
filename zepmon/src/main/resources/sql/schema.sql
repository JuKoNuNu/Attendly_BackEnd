-- DB
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

-- 2. 모니터링 이벤트
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

-- 3. 자리비움 세션
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

-- 테스트 데이터
INSERT IGNORE INTO student_master (student_id, name, cohort, asset_code) VALUES
('LGCNS 5기 - 이주형 A', '이주형', 'LGCNS 5기', 'NB-001'),
('LGCNS 5기 - 이주형 B', '이주형', 'LGCNS 5기', 'NB-002'),
('LGCNS 5기 - 김철수', '김철수', 'LGCNS 5기', 'NB-003'),
('LGCNS 5기 - 박민수', '박민수', 'LGCNS 5기', 'NB-004'),
('LGCNS 5기 - 최지원', '최지원', 'LGCNS 5기', 'NB-005');
