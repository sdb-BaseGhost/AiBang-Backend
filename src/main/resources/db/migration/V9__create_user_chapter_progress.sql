-- 用户章节进度表
CREATE TABLE user_chapter_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    score INT COMMENT '练习得分（100分制）',
    status VARCHAR(20) DEFAULT 'NOT_STARTED' COMMENT '状态：NOT_STARTED-未开始，COMPLETED-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_user_chapter (user_id, chapter_id),
    INDEX idx_user_id (user_id),
    INDEX idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户章节进度表';
