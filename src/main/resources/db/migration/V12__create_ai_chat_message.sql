-- V12: 创建AI辅导消息表
USE aiban;

CREATE TABLE ai_chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '会话ID（关联ai_chat_session.id）',
    role VARCHAR(16) NOT NULL COMMENT '角色：user/assistant',
    content LONGTEXT NOT NULL COMMENT '消息内容',
    dify_message_id VARCHAR(64) DEFAULT NULL COMMENT 'Dify消息ID',
    token_count INT DEFAULT 0 COMMENT '消耗token数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI辅导消息表';
