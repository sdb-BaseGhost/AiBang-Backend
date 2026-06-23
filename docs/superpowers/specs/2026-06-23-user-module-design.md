# 设计文档：模块1 - 用户体系（核心）

> 日期：2026-06-23
> 范围：注册/登录/Token刷新/获取当前用户/个人资料/密码修改/头像上传（7个接口）

---

## 1. 目标

实现用户体系的核心功能，包括认证（注册、登录、Token管理）和用户信息管理（资料更新、密码修改、头像上传），为后续业务模块提供用户身份支撑。

## 2. 范围

| 序号 | 接口 | 方法 | 说明 | 状态 |
|------|------|------|------|------|
| 1 | POST /api/auth/register | register | 用户注册 | ✅ |
| 2 | POST /api/auth/login | login | 用户登录 | ✅ |
| 3 | POST /api/auth/refresh | refreshToken | 刷新Token | ✅ |
| 4 | GET /api/auth/me | getCurrentUser | 获取当前用户 | ✅ |
| 5 | PUT /api/user/profile | updateProfile | 更新个人资料 | ✅ |
| 6 | PUT /api/user/password | changePassword | 修改密码 | ✅ |
| 7 | POST /api/file/avatar | uploadAvatar | 上传头像 | ✅ |

**不在本次范围：**
- 管理员接口（用户列表/详情/角色修改/禁用启用）→ 后续实现
- 简历上传 → 后续实现

## 3. 技术选型决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 密码加密 | BCrypt | Spring Security 默认支持，安全性高 |
| 注册验证 | 无验证 | 实训项目够用，减少复杂度 |
| 文件存储 | 本地存储 | 无云服务依赖，uploads/ 目录映射 |
| 手机号脱敏 | 后端处理 | 返回时脱敏，存储完整号码 |

## 4. 包结构

```
src/main/java/org/sdb/aiban/
├── controller/
│   ├── AuthController.java
│   └── UserController.java
├── service/
│   ├── AuthService.java
│   └── UserService.java
├── mapper/
│   └── UserMapper.java
├── entity/
│   └── SysUser.java
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── UpdateProfileRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   └── UploadAvatarRequest.java
│   └── response/
│       ├── LoginResponse.java
│       ├── UserResponse.java
│       └── FileUploadResponse.java
└── common/
    ├── result/ResultCode.java      # 扩展
    └── constant/UserConstants.java  # 新增
    └── validator/UserValidator.java # 新增
```

## 5. 数据库设计

### sys_user 表

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
```

## 6. 详细设计

### 6.1 Auth 模块

#### AuthController

| 接口 | 方法 | 入参 | 出参 | 说明 |
|------|------|------|------|------|
| POST /api/auth/register | register | RegisterRequest | Result<UserVO> | 注册 |
| POST /api/auth/login | login | LoginRequest | Result<LoginResponse> | 登录 |
| POST /api/auth/refresh | refreshToken | RefreshTokenRequest | Result<LoginResponse> | 刷新Token |
| GET /api/auth/me | getCurrentUser | - | Result<UserResponse> | 获取当前用户 |

#### AuthService

**register(RegisterRequest)**
1. 校验用户名格式（4-20位，字母数字下划线）
2. 校验密码强度（6-20位，含字母和数字）
3. 校验两次密码一致
4. 校验用户名唯一性
5. 密码 BCrypt 加密
6. 插入数据库（默认角色 STUDENT，状态 ACTIVE）
7. 返回 UserVO

**login(LoginRequest)**
1. 查询用户是否存在
2. 校验密码（BCrypt）
3. 校验账号状态（ACTIVE）
4. 更新 lastLoginTime
5. 生成 accessToken（2小时）+ refreshToken（7天）
6. 返回 LoginResponse

**refreshToken(RefreshTokenRequest)**
1. 解析 refreshToken
2. 校验类型为 refresh
3. 查询用户是否存在
4. 生成新的 accessToken + refreshToken
5. 返回 LoginResponse

**getCurrentUser(Long userId)**
1. 查询用户信息
2. 手机号脱敏
3. 返回 UserResponse

### 6.2 User 模块

#### UserController

| 接口 | 方法 | 入参 | 出参 | 说明 |
|------|------|------|------|------|
| PUT /api/user/profile | updateProfile | UpdateProfileRequest | Result<UserResponse> | 更新个人资料 |
| PUT /api/user/password | changePassword | ChangePasswordRequest | Result<Void> | 修改密码 |
| POST /api/file/avatar | uploadAvatar | MultipartFile | Result<FileUploadResponse> | 上传头像 |

#### UserService

**updateProfile(Long userId, UpdateProfileRequest)**
1. 查询用户存在
2. 更新非空字段
3. 返回 UserResponse（手机号脱敏）

**changePassword(Long userId, ChangePasswordRequest)**
1. 查询用户存在
2. 校验原密码（BCrypt）
3. 校验新密码强度
4. 校验新密码与原密码不同
5. 校验两次密码一致
6. 更新密码

**uploadAvatar(Long userId, MultipartFile)**
1. 校验文件类型（jpg/png/gif）
2. 校验文件大小（5MB）
3. 生成存储路径（uploads/avatars/{userId}/{yyyy}/{MM}/{uuid}.{ext}）
4. 保存文件
5. 更新用户 avatar 字段
6. 返回 FileUploadResponse

### 6.3 错误码扩展

| 枚举值 | code | message | 使用场景 |
|--------|------|---------|----------|
| USER_NOT_FOUND | 1001 | 用户不存在 | 登录/查询 |
| USERNAME_EXISTS | 1002 | 用户名已被注册 | 注册 |
| PASSWORD_ERROR | 1003 | 用户名或密码错误 | 登录 |
| ACCOUNT_DISABLED | 1004 | 账号已被禁用 | 登录 |
| USERNAME_INVALID | 1005 | 用户名格式不正确 | 注册 |
| PASSWORD_INVALID | 1006 | 密码格式不正确 | 注册/修改密码 |
| PASSWORD_MISMATCH | 1007 | 两次输入的密码不一致 | 注册/修改密码 |
| OLD_PASSWORD_ERROR | 1008 | 原密码不正确 | 修改密码 |
| SAME_PASSWORD | 1009 | 新密码不能与原密码相同 | 修改密码 |
| FILE_TOO_LARGE | 1010 | 文件大小超过限制 | 上传文件 |
| FILE_TYPE_INVALID | 1011 | 文件类型不支持 | 上传文件 |

### 6.4 请求流程

#### 注册流程

```
1. POST /api/auth/register
   Body: { "username": "zhangsan", "password": "Abc12345", "confirmPassword": "Abc12345" }

2. AuthController.register()
   → 参数校验（@Valid）

3. AuthService.register()
   → UserValidator.validateUsername()
   → UserValidator.validatePassword()
   → 校验两次密码一致
   → UserMapper.selectByUsername() → 校验用户名唯一
   → passwordEncoder.encode() → 密码加密
   → SysUser user = new SysUser() → 设置默认值
   → UserMapper.insert()
   → 返回 UserVO
```

#### 登录流程

```
1. POST /api/auth/login
   Body: { "username": "zhangsan", "password": "Abc12345" }

2. AuthController.login()
   → 参数校验（@Valid）

3. AuthService.login()
   → UserMapper.selectByUsername()
   → 校验用户存在
   → passwordEncoder.matches() → 校验密码
   → 校验账号状态
   → UserMapper.updateLastLoginTime()
   → jwtUtils.generateAccessToken()
   → jwtUtils.generateRefreshToken()
   → 构建 LoginResponse
```

## 7. 不做什么

- 不实现管理员接口（用户列表/详情/角色修改/禁用启用）
- 不实现简历上传
- 不做邮箱/手机验证
- 不做第三方登录（微信、QQ等）
- 不做登录日志记录

## 8. 验收标准

| 验收项 | 正例 | 反例 |
|--------|------|------|
| 注册 | 用户名"zhangsan"→ 注册成功，返回userId | 用户名已存在→返回1002 |
| 注册 | 密码"Abc12345"→ 注册成功 | 密码"123456"→返回1006（无字母） |
| 登录 | 正确用户名密码→ 返回token对 | 密码错误→返回1003 |
| 登录 | 正常账号→ 登录成功 | 禁用账号→返回1004 |
| Token刷新 | 有效refreshToken→ 返回新token对 | 过期refreshToken→返回401 |
| 获取当前用户 | 有效token→ 返回用户信息 | 无token→返回401 |
| 更新资料 | 更新nickname→ 返回更新后信息 | nickname超20字→返回400 |
| 修改密码 | 正确原密码→ 修改成功 | 原密码错误→返回1008 |
| 上传头像 | 5MB jpg→ 上传成功 | 6MB文件→返回1010 |
| 上传头像 | png文件→ 上传成功 | exe文件→返回1011 |

## 9. 风险

| 风险 | 缓解措施 |
|------|----------|
| BCrypt加密耗时 | 注册/登录单次请求可接受 |
| 文件上传导致磁盘满 | 限制文件大小，监控磁盘空间 |
| JWT密钥泄露 | 仅限开发阶段硬编码 |
| 并发注册用户名冲突 | 数据库唯一索引保证 |
