-- 技能章节表
CREATE TABLE skill_chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '章节ID',
    skill_id BIGINT NOT NULL COMMENT '所属技能ID',
    title VARCHAR(200) NOT NULL COMMENT '章节标题',
    content TEXT COMMENT '章节学习内容（Markdown格式）',
    sort_order INT DEFAULT 0 COMMENT '排序顺序，越小越靠前',
    status VARCHAR(20) DEFAULT 'PUBLISHED' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_skill_id (skill_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能章节表';
