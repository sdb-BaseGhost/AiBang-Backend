-- V13: 创建简历表
USE aiban;

CREATE TABLE ai_resume (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID（关联sys_user.id）',
    title VARCHAR(200) NOT NULL COMMENT '简历标题（文件名）',
    original_content LONGTEXT NOT NULL COMMENT 'PDF提取的原始文本',
    optimized_content LONGTEXT DEFAULT NULL COMMENT 'Dify优化后的Markdown内容',
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED' COMMENT '状态：UPLOADED-已上传 ANALYZING-分析中 ANALYZED-已分析 ANALYZE_FAILED-分析失败',
    error_message VARCHAR(500) DEFAULT NULL COMMENT '错误信息（分析失败时）',
    task_id VARCHAR(64) DEFAULT NULL COMMENT 'Dify任务ID',
    workflow_run_id VARCHAR(64) DEFAULT NULL COMMENT 'Dify工作流运行ID',
    elapsed_time DECIMAL(10,2) DEFAULT NULL COMMENT '耗时（秒）',
    total_tokens INT DEFAULT 0 COMMENT '消耗token数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='简历表';
