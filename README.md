# AI伴学与职业成长平台 (AiBan)

高校学生 AI 伴学与职业成长平台后端服务。提供用户体系、技能树管理、学习数据看板、学习打卡、管理员后台等功能。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Java 17 + Spring Boot 3.4.7 |
| 安全认证 | Spring Security + JWT (jjwt 0.12.6) |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.0+ |
| API 文档 | Knife4j 4.5.0 (OpenAPI 3) |
| 工具库 | Lombok、Apache POI (Excel导入导出) |
| 构建工具 | Maven |

## 项目结构

```
src/main/java/org/sdb/aiban/
├── AibanApplication.java          # 启动类
├── common/
│   ├── constant/                  # 常量
│   ├── exception/                 # 全局异常处理
│   ├── result/                    # 统一返回结果
│   └── validator/                 # 参数校验
├── config/                        # 配置类 (CORS、MyBatis-Plus、WebMvc)
├── controller/                    # 接口层
│   ├── AuthController.java        # 认证 (注册/登录/Token刷新)
│   ├── UserController.java        # 用户 (资料/密码/头像)
│   ├── AdminController.java       # 管理员 (仪表盘/用户管理)
│   ├── SkillController.java       # 技能树
│   ├── LearningController.java    # 学习数据看板
│   └── CheckinController.java     # 学习打卡
├── dto/                           # 数据传输对象
│   ├── request/                   # 请求 DTO
│   └── response/                  # 响应 VO
├── entity/                        # 实体类
├── mapper/                        # MyBatis-Plus Mapper
├── security/                      # JWT + Spring Security
└── service/                       # 业务逻辑层
```

## 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/sdb-BaseGhost/AiBang-Backend.git
cd AiBang-Backend
```

### 2. 创建数据库

```bash
mysql -u root -p -e "CREATE DATABASE aiban DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"
```

### 3. 执行建表 SQL

```bash
mysql -u root -p aiban < src/main/resources/db/migration/V1__create_sys_user.sql
mysql -u root -p aiban < src/main/resources/db/migration/V2__create_user_skill_progress.sql
mysql -u root -p aiban < src/main/resources/db/migration/V3__create_learning_record.sql
mysql -u root -p aiban < src/main/resources/db/migration/V4__create_user_checkin.sql
```

### 4. 修改数据库配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiban?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
    username: root
    password: "你的密码"
```

### 5. 编译运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/AIBAN-0.0.1-SNAPSHOT.jar

# 或直接使用 Maven 运行
mvn spring-boot:run
```

### 6. 验证服务

- 后端服务：http://localhost:8081
- Knife4j API 文档：http://localhost:8081/doc.html

### 7. 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | ADMIN |
| student | 123456 | STUDENT |

## 数据库建表语句

### sys_user - 用户表

```sql
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色：STUDENT/ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    bio VARCHAR(500) DEFAULT '' COMMENT '个人简介',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 测试数据（密码均为 123456）
INSERT INTO sys_user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'ADMIN', 'ACTIVE'),
('student', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试学生', 'STUDENT', 'ACTIVE');
```

### user_skill_progress - 用户技能进度表

```sql
CREATE TABLE user_skill_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    skill_id BIGINT NOT NULL COMMENT '技能ID',
    progress INT NOT NULL DEFAULT 0 COMMENT '进度 0-100',
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/IN_PROGRESS/COMPLETED',
    rating INT DEFAULT 0 COMMENT '自评等级 1-5',
    note VARCHAR(500) DEFAULT '' COMMENT '学习笔记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_skill (user_id, skill_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能进度表';
```

### learning_record - 学习记录表

```sql
CREATE TABLE learning_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：SKILL_STUDY/AI_CHAT/RESUME/CHECKIN',
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    duration INT NOT NULL DEFAULT 0 COMMENT '时长（分钟）',
    detail VARCHAR(500) DEFAULT '' COMMENT '详细描述',
    skill_id BIGINT DEFAULT NULL COMMENT '关联技能ID（可选）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';
```

### user_checkin - 用户打卡表

```sql
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
```

## 接口列表

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/refresh` | 刷新 Token | 否 |
| GET  | `/api/auth/me` | 获取当前用户信息 | 是 |

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| PUT  | `/api/user/profile` | 更新个人资料 | 是 |
| PUT  | `/api/user/password` | 修改密码 | 是 |
| POST | `/api/user/avatar` | 上传头像 | 是 |

### 管理员模块 `/api/admin`（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/admin/dashboard` | 获取仪表盘统计数据 |
| GET  | `/api/admin/users` | 获取用户列表（分页） |
| GET  | `/api/admin/users/{id}` | 获取用户详情 |
| PUT  | `/api/admin/users/{id}/role` | 修改用户角色 |
| PUT  | `/api/admin/users/{id}/status` | 启用/禁用用户 |
| POST | `/api/admin/users/{id}/reset-password` | 重置用户密码 |
| POST | `/api/admin/user/import` | 批量导入用户（Excel） |
| GET  | `/api/admin/user/export` | 导出用户数据（Excel） |

### 技能树模块 `/api/skill`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET  | `/api/skill/categories` | 获取技能分类列表 | 否 |
| GET  | `/api/skill/tree` | 获取技能树 | 否 |
| GET  | `/api/skill/user/progress` | 获取我的技能进度 | 是 |
| PUT  | `/api/skill/user/progress` | 更新技能进度 | 是 |

### 学习数据看板 `/api/learning`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET  | `/api/learning/dashboard` | 获取学习仪表盘数据 | 是 |
| GET  | `/api/learning/timeline` | 获取学习时间线 | 是 |
| GET  | `/api/learning/report/weekly` | 获取周报 | 是 |
| GET  | `/api/learning/report/monthly` | 获取月报 | 是 |

### 学习打卡 `/api/checkin`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/checkin` | 今日打卡 | 是 |
| GET  | `/api/checkin/records` | 获取打卡记录 | 是 |

## 已实现功能

### P0 核心功能

- **用户体系**：注册、登录、JWT 认证、Token 刷新、个人资料管理、密码修改、头像上传
- **技能树管理**：技能分类展示、树形技能结构、进度更新（含自评等级和笔记）
- **学习数据看板**：学习仪表盘、学习时间线（分页）、周报、月报统计
- **学习打卡**：每日打卡、连续天数记录、打卡日历查询
- **管理员后台**：仪表盘统计、用户 CRUD（列表/详情/角色/状态/重置密码）、Excel 批量导入导出

### 公共能力

- 统一返回格式 `Result<T>` + 错误码枚举
- 全局异常处理 `GlobalExceptionHandler`
- Spring Security + JWT 无状态认证授权
- CORS 跨域配置
- MyBatis-Plus 逻辑删除 + 驼峰映射
- Knife4j API 文档自动生成

## 待实现功能

### P1 扩展功能

- **AI 学业辅导**：集成 Dify Workflow/Chatflow，实现智能问答和学业咨询
- **简历诊断优化**：简历上传、AI 分析评分、优化建议生成
- **学习路径推荐**：基于技能进度和目标智能推荐学习路径
- **成长报告生成**：自动生成阶段性成长分析报告
- **职业规划**：职业目标设定、技能差距分析、岗位匹配推荐

### P2 创新功能

- **社区功能**：学习动态发布、评论互动、经验分享
- **排行榜**：学习时长排行、技能完成度排行
- **徽章系统**：成就徽章解锁、学习里程碑激励
- **消息通知**：系统消息推送、打卡提醒
- **数据分析**：学习行为深度分析、个性化学习建议

## 配置说明

主要配置项在 `application.yml`：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8081 | 服务端口 |
| `spring.datasource.url` | `localhost:3306/aiban` | 数据库地址 |
| `jwt.secret` | - | JWT 签名密钥 |
| `jwt.access-token-expiration` | 7200000 (2h) | Access Token 过期时间 |
| `jwt.refresh-token-expiration` | 604800000 (7d) | Refresh Token 过期时间 |
| `file.upload-path` | `./` | 文件上传路径 |

## License

MIT
