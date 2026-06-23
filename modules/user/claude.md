# 模块1：用户体系 - 开发规范

## 模块定位

实现用户认证（注册、登录、Token管理）和用户信息管理（资料更新、密码修改、头像上传），为后续业务模块提供用户身份支撑。

## 技术栈

- Spring Boot 3 + Spring Security + MyBatis-Plus
- JWT (jjwt 0.12.6) + BCrypt 密码加密
- MySQL 8.0 + 本地文件存储

## 接口清单

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| POST /api/auth/register | register | 用户注册 | ❌ |
| POST /api/auth/login | login | 用户登录 | ❌ |
| POST /api/auth/refresh | refreshToken | 刷新Token | ❌ |
| GET /api/auth/me | getCurrentUser | 获取当前用户 | ✅ |
| PUT /api/user/profile | updateProfile | 更新个人资料 | ✅ |
| PUT /api/user/password | changePassword | 修改密码 | ✅ |
| POST /api/file/avatar | uploadAvatar | 上传头像 | ✅ |

## 包结构

```
src/main/java/org/sdb/aiban/
├── controller/
│   ├── AuthController.java          # 认证接口
│   └── UserController.java          # 用户接口
├── service/
│   ├── AuthService.java             # 认证业务
│   └── UserService.java             # 用户业务
├── mapper/
│   └── UserMapper.java              # 用户数据访问
├── entity/
│   └── SysUser.java                 # 用户实体
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
    ├── constant/UserConstants.java
    └── validator/UserValidator.java
```

## 数据库

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
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
```

## 业务规则

### 注册

1. 用户名：4-20位，只允许字母、数字、下划线，唯一性校验
2. 密码：6-20位，必须包含字母和数字
3. 两次密码一致性校验
4. 默认角色 STUDENT，状态 ACTIVE
5. 密码 BCrypt 加密存储

### 登录

1. 用户名存在性校验
2. 密码 BCrypt 比对
3. 账号状态校验（ACTIVE/DISABLED）
4. 更新 lastLoginTime
5. 返回 accessToken（2小时）+ refreshToken（7天）

### Token刷新

1. refreshToken 有效性校验
2. refreshToken 类型校验（必须是 refresh）
3. 生成新的 accessToken + refreshToken（一次性使用）

### 上传头像

1. 文件类型：jpg, png, gif
2. 文件大小：最大 5MB
3. 存储路径：uploads/avatars/{userId}/{yyyy}/{MM}/{uuid}.{ext}
4. 更新用户 avatar 字段

## 错误码

| 枚举值 | code | message |
|--------|------|---------|
| USER_NOT_FOUND | 1001 | 用户不存在 |
| USERNAME_EXISTS | 1002 | 用户名已被注册 |
| PASSWORD_ERROR | 1003 | 用户名或密码错误 |
| ACCOUNT_DISABLED | 1004 | 账号已被禁用 |
| USERNAME_INVALID | 1005 | 用户名格式不正确 |
| PASSWORD_INVALID | 1006 | 密码格式不正确 |
| PASSWORD_MISMATCH | 1007 | 两次输入的密码不一致 |
| OLD_PASSWORD_ERROR | 1008 | 原密码不正确 |
| SAME_PASSWORD | 1009 | 新密码不能与原密码相同 |
| FILE_TOO_LARGE | 1010 | 文件大小超过限制 |
| FILE_TYPE_INVALID | 1011 | 文件类型不支持 |

## 开发流程

1. 先创建 sys_user 表
2. 创建 Entity、Mapper
3. 创建 DTO（Request/Response）
4. 创建 Service 层业务逻辑
5. 创建 Controller 层接口
6. 测试所有接口
7. 提交代码

## 测试账号

- admin / 123456 (ADMIN角色)
- student / 123456 (STUDENT角色)

## 注意事项

- 手机号返回时需要脱敏：138****8000
- 密码字段不能出现在响应中
- 文件上传使用 MultipartFile，不是 RequestBody
- JWT token 从 Authorization: Bearer xxx 头获取
