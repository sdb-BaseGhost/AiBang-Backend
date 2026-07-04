CREATE TABLE daily_study_duration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    study_date DATE NOT NULL COMMENT '学习日期',
    duration_minutes INT NOT NULL DEFAULT 0 COMMENT '学习时长（分钟）：30/60/90/120',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_date (user_id, study_date),
    INDEX idx_user_date (user_id, study_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日学习时长表';
