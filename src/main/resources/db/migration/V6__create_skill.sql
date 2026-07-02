-- 技能表
CREATE TABLE skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '技能名称',
    category_id BIGINT NOT NULL COMMENT '所属分类ID',
    level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER' COMMENT '难度级别：BEGINNER/INTERMEDIATE/ADVANCED/EXPERT',
    description VARCHAR(1000) DEFAULT '' COMMENT '技能描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序，越大越靠前',
    parent_skill_id BIGINT DEFAULT NULL COMMENT '父技能ID，用于构建树形结构',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_category_id (category_id),
    INDEX idx_parent_skill_id (parent_skill_id),
    INDEX idx_level (level),
    FOREIGN KEY (category_id) REFERENCES skill_category(id) ON DELETE RESTRICT,
    FOREIGN KEY (parent_skill_id) REFERENCES skill(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='技能表';

-- 插入测试数据
INSERT INTO skill (name, category_id, level, description, sort_order, parent_skill_id) VALUES
('Java', 1, 'BEGINNER', 'Java 编程语言核心技能', 1, NULL),
('Java 基础', 1, 'BEGINNER', 'Java 语法、面向对象、集合框架等', 2, 1),
('Java 进阶', 1, 'INTERMEDIATE', '多线程、IO、网络编程等', 3, 1),
('Python', 1, 'BEGINNER', 'Python 编程语言核心技能', 4, NULL),
('HTML/CSS', 2, 'BEGINNER', '网页基础标记和样式', 1, NULL),
('JavaScript', 2, 'BEGINNER', '网页交互脚本语言', 2, NULL),
('Vue.js', 2, 'INTERMEDIATE', 'Vue.js 前端框架', 3, 6),
('React', 2, 'INTERMEDIATE', 'React 前端框架', 4, 6),
('Spring Boot', 3, 'INTERMEDIATE', 'Spring Boot 后端框架', 1, NULL),
('MySQL', 4, 'BEGINNER', 'MySQL 关系型数据库', 1, NULL),
('Redis', 4, 'INTERMEDIATE', 'Redis 缓存数据库', 2, NULL),
('机器学习基础', 5, 'BEGINNER', '机器学习基本概念和算法', 1, NULL);