-- 用户打卡表

CREATE TABLE user_checkin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    duration INT NOT NULL DEFAULT 0 COMMENT '学习时长（分钟）',
    note VARCHAR(200) DEFAULT '' COMMENT '学习笔记',
    streak INT NOT NULL DEFAULT 1 COMMENT '连续打卡天数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_date (user_id, checkin_date),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户打卡表';
