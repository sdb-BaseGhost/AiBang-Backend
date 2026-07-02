-- V11: 创建AI辅导会话表
USE aiban;

CREATE TABLE ai_chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID（关联sys_user.id）',
    title VARCHAR(200) DEFAULT '' COMMENT '会话标题（取首条消息前20字）',
    dify_conversation_id VARCHAR(64) DEFAULT NULL COMMENT 'Dify会话ID（首条消息后由Dify返回）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-进行中 1-已归档',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI辅导会话表';
