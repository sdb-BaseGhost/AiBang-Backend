-- 技能分类表
CREATE TABLE skill_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(50) DEFAULT '' COMMENT '图标标识',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序，越大越靠前',
    description VARCHAR(500) DEFAULT '' COMMENT '分类描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='技能分类表';

-- 插入测试数据
INSERT INTO skill_category (name, icon, sort_order, description) VALUES
('编程语言', 'code', 1, '各种编程语言的学习'),
('前端开发', 'web', 2, '前端开发相关技术'),
('后端开发', 'server', 3, '后端开发相关技术'),
('数据库', 'database', 4, '数据库技术'),
('AI/机器学习', 'ai', 5, '人工智能与机器学习');