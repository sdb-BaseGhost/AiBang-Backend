-- 章节练习题表
CREATE TABLE chapter_exercise (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '练习题ID',
    chapter_id BIGINT NOT NULL COMMENT '所属章节ID',
    question TEXT NOT NULL COMMENT '题目内容',
    options JSON NOT NULL COMMENT '选项列表 ["选项A", "选项B", "选项C", "选项D"]',
    answer INT NOT NULL COMMENT '正确答案索引（0-3）',
    explanation TEXT COMMENT '答案解析',
    sort_order INT DEFAULT 0 COMMENT '排序顺序，越小越靠前',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节练习题表';
