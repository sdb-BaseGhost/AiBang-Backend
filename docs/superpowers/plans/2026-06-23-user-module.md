# 模块1 - 用户体系 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现用户体系核心功能，包括注册、登录、Token管理、个人资料更新、密码修改和头像上传。

**Architecture:** 标准三层架构 Controller → Service → Mapper，DTO 分离请求响应，统一 Result<T> 返回。

**Tech Stack:** Spring Boot 3 · Spring Security · MyBatis-Plus · JWT (jjwt) · BCrypt · MySQL 8.0

---

## File Structure

```
src/main/java/org/sdb/aiban/
├── controller/
│   ├── AuthController.java                    # CREATE
│   └── UserController.java                    # CREATE
├── service/
│   ├── AuthService.java                       # CREATE
│   └── UserService.java                       # CREATE
├── mapper/
│   └── UserMapper.java                        # CREATE
├── entity/
│   └── SysUser.java                           # CREATE
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java               # CREATE
│   │   ├── LoginRequest.java                  # CREATE
│   │   ├── RefreshTokenRequest.java           # CREATE
│   │   ├── UpdateProfileRequest.java          # CREATE
│   │   ├── ChangePasswordRequest.java         # CREATE
│   │   └── UploadAvatarRequest.java           # CREATE
│   └── response/
│       ├── LoginResponse.java                 # CREATE
│       ├── UserVO.java                        # CREATE
│       ├── UserResponse.java                  # CREATE
│       └── FileUploadResponse.java            # CREATE
└── common/
    ├── constant/
    │   └── UserConstants.java                 # CREATE
    └── validator/
        └── UserValidator.java                 # CREATE
```

---

### Task 1: 创建 sys_user 数据库表

**Files:**
- Create: `src/main/resources/db/migration/V1__create_sys_user.sql`

- [ ] **Step 1: 创建建表 SQL**

```sql
-- src/main/resources/db/migration/V1__create_sys_user.sql

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

-- 插入测试数据（密码为 123456 的 BCrypt 加密）
INSERT INTO sys_user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'ADMIN', 'ACTIVE'),
('student', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试学生', 'STUDENT', 'ACTIVE');
```

- [ ] **Step 2: 执行 SQL**

Run: `mysql -u root -p1234 aiban < src/main/resources/db/migration/V1__create_sys_user.sql`

- [ ] **Step 3: 验证数据**

Run: `mysql -u root -p1234 aiban -e "SELECT id, username, nickname, role FROM sys_user;"`
Expected:
```
+----+----------+----------+---------+
| id | username | nickname | role    |
+----+----------+----------+---------+
|  1 | admin    | 管理员   | ADMIN   |
|  2 | student  | 测试学生 | STUDENT |
+----+----------+----------+---------+
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/migration/V1__create_sys_user.sql
git commit -m "feat(user): add sys_user table with test data"
```

---

### Task 2: 创建 Entity 和 Mapper

**Files:**
- Create: `src/main/java/org/sdb/aiban/entity/SysUser.java`
- Create: `src/main/java/org/sdb/aiban/mapper/UserMapper.java`

- [ ] **Step 1: 创建 SysUser.java**

```java
package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String bio;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
}
```

- [ ] **Step 2: 创建 UserMapper.java**

```java
package org.sdb.aiban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sdb.aiban.entity.SysUser;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/sdb/aiban/entity/SysUser.java src/main/java/org/sdb/aiban/mapper/UserMapper.java
git commit -m "feat(user): add SysUser entity and UserMapper"
```

---

### Task 3: 创建 DTO - Request

**Files:**
- Create: `src/main/java/org/sdb/aiban/dto/request/RegisterRequest.java`
- Create: `src/main/java/org/sdb/aiban/dto/request/LoginRequest.java`
- Create: `src/main/java/org/sdb/aiban/dto/request/RefreshTokenRequest.java`
- Create: `src/main/java/org/sdb/aiban/dto/request/UpdateProfileRequest.java`
- Create: `src/main/java/org/sdb/aiban/dto/request/ChangePasswordRequest.java`

- [ ] **Step 1: 创建 RegisterRequest.java**

```java
package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度4-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只允许字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    private String nickname;
    private String email;
    private String phone;
}
```

- [ ] **Step 2: 创建 LoginRequest.java**

```java
package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 3: 创建 RefreshTokenRequest.java**

```java
package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
```

- [ ] **Step 4: 创建 UpdateProfileRequest.java**

```java
package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 20, message = "昵称不超过20字")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    @Size(max = 200, message = "个人简介不超过200字")
    private String bio;
}
```

- [ ] **Step 5: 创建 ChangePasswordRequest.java**

```java
package org.sdb.aiban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
```

- [ ] **Step 6: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/sdb/aiban/dto/request/
git commit -m "feat(user): add request DTOs for auth and user"
```

---

### Task 4: 创建 DTO - Response

**Files:**
- Create: `src/main/java/org/sdb/aiban/dto/response/UserVO.java`
- Create: `src/main/java/org/sdb/aiban/dto/response/LoginResponse.java`
- Create: `src/main/java/org/sdb/aiban/dto/response/UserResponse.java`
- Create: `src/main/java/org/sdb/aiban/dto/response/FileUploadResponse.java`

- [ ] **Step 1: 创建 UserVO.java**

```java
package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
}
```

- [ ] **Step 2: 创建 LoginResponse.java**

```java
package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserVO user;
}
```

- [ ] **Step 3: 创建 UserResponse.java**

```java
package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String role;
    private String bio;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 创建 FileUploadResponse.java**

```java
package org.sdb.aiban.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FileUploadResponse {
    private String url;
    private String filename;
}
```

- [ ] **Step 5: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/sdb/aiban/dto/response/
git commit -m "feat(user): add response DTOs for auth, user and file"
```

---

### Task 5: 创建常量和校验工具

**Files:**
- Create: `src/main/java/org/sdb/aiban/common/constant/UserConstants.java`
- Create: `src/main/java/org/sdb/aiban/common/validator/UserValidator.java`

- [ ] **Step 1: 创建 UserConstants.java**

```java
package org.sdb.aiban.common.constant;

import java.util.List;

public class UserConstants {

    // 用户名规则
    public static final int USERNAME_MIN_LENGTH = 4;
    public static final int USERNAME_MAX_LENGTH = 20;
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";

    // 密码规则
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).+$";

    // 昵称规则
    public static final int NICKNAME_MAX_LENGTH = 20;

    // 个人简介规则
    public static final int BIO_MAX_LENGTH = 200;

    // 文件上传规则
    public static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024; // 5MB
    public static final List<String> AVATAR_ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif");

    // 用户状态
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISABLED = "DISABLED";

    // 用户角色
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMIN = "ADMIN";
}
```

- [ ] **Step 2: 创建 UserValidator.java**

```java
package org.sdb.aiban.common.validator;

import org.sdb.aiban.common.constant.UserConstants;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UserValidator {

    public void validateUsername(String username) {
        if (username == null || username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new BusinessException(ResultCode.USERNAME_INVALID, "用户名长度4-20位");
        }
        if (!username.matches(UserConstants.USERNAME_PATTERN)) {
            throw new BusinessException(ResultCode.USERNAME_INVALID, "用户名只允许字母、数字、下划线");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new BusinessException(ResultCode.PASSWORD_INVALID, "密码长度6-20位");
        }
        if (!password.matches(UserConstants.PASSWORD_PATTERN)) {
            throw new BusinessException(ResultCode.PASSWORD_INVALID, "密码必须包含字母和数字");
        }
    }

    public void validateAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要上传的文件");
        }
        if (file.getSize() > UserConstants.AVATAR_MAX_SIZE) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE, "文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !UserConstants.AVATAR_ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_INVALID, "只支持jpg、png、gif格式");
        }
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/sdb/aiban/common/constant/UserConstants.java src/main/java/org/sdb/aiban/common/validator/UserValidator.java
git commit -m "feat(user): add UserConstants and UserValidator"
```

---

### Task 6: 扩展 ResultCode

**Files:**
- Modify: `src/main/java/org/sdb/aiban/common/result/ResultCode.java`

- [ ] **Step 1: 添加用户模块错误码**

在 `ResultCode.java` 中添加：

```java
// 在 ACCOUNT_DISABLED 后面添加
USERNAME_INVALID(1005, "用户名格式不正确"),
PASSWORD_INVALID(1006, "密码格式不正确"),
PASSWORD_MISMATCH(1007, "两次输入的密码不一致"),
OLD_PASSWORD_ERROR(1008, "原密码不正确"),
SAME_PASSWORD(1009, "新密码不能与原密码相同"),
FILE_TOO_LARGE(1010, "文件大小超过限制"),
FILE_TYPE_INVALID(1011, "文件类型不支持");
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/common/result/ResultCode.java
git commit -m "feat(user): extend ResultCode with user module error codes"
```

---

### Task 7: 创建 AuthService

**Files:**
- Create: `src/main/java/org/sdb/aiban/service/AuthService.java`

- [ ] **Step 1: 创建 AuthService.java**

```java
package org.sdb.aiban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.constant.UserConstants;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.common.validator.UserValidator;
import org.sdb.aiban.dto.request.LoginRequest;
import org.sdb.aiban.dto.request.RefreshTokenRequest;
import org.sdb.aiban.dto.request.RegisterRequest;
import org.sdb.aiban.dto.response.LoginResponse;
import org.sdb.aiban.dto.response.UserVO;
import org.sdb.aiban.entity.SysUser;
import org.sdb.aiban.mapper.UserMapper;
import org.sdb.aiban.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserValidator userValidator;

    public UserVO register(RegisterRequest request) {
        // 1. 校验用户名格式
        userValidator.validateUsername(request.getUsername());

        // 2. 校验密码强度
        userValidator.validatePassword(request.getPassword());

        // 3. 校验两次密码一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_MISMATCH);
        }

        // 4. 校验用户名唯一性
        SysUser existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (existingUser != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 5. 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail() != null ? request.getEmail() : "");
        user.setPhone(request.getPhone() != null ? request.getPhone() : "");
        user.setRole(UserConstants.ROLE_STUDENT);
        user.setStatus(UserConstants.STATUS_ACTIVE);
        user.setBio("");
        userMapper.insert(user);

        log.info("用户注册成功: {}", user.getUsername());

        return UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 3. 校验账号状态
        if (UserConstants.STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 生成 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户登录成功: {}", user.getUsername());

        UserVO userVO = UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(UserConstants.ACCESS_TOKEN_EXPIRATION / 1000)
                .user(userVO)
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // 1. 解析 refreshToken
        if (!jwtUtils.validateToken(request.getRefreshToken())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        var claims = jwtUtils.parseToken(request.getRefreshToken());
        String type = jwtUtils.getTokenType(claims);
        if (!"refresh".equals(type)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 2. 查询用户
        Long userId = jwtUtils.getUserId(claims);
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 生成新 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        UserVO userVO = UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(UserConstants.ACCESS_TOKEN_EXPIRATION / 1000)
                .user(userVO)
                .build();
    }

    public UserVO getCurrentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/service/AuthService.java
git commit -m "feat(user): implement AuthService with register, login, refresh, getCurrentUser"
```

---

### Task 8: 创建 AuthController

**Files:**
- Create: `src/main/java/org/sdb/aiban/controller/AuthController.java`

- [ ] **Step 1: 创建 AuthController.java**

```java
package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.LoginRequest;
import org.sdb.aiban.dto.request.RefreshTokenRequest;
import org.sdb.aiban.dto.request.RegisterRequest;
import org.sdb.aiban.dto.response.LoginResponse;
import org.sdb.aiban.dto.response.UserVO;
import org.sdb.aiban.security.JwtUtils;
import org.sdb.aiban.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "注册、登录、Token刷新")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        UserVO userVO = authService.register(request);
        return Result.success(userVO);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return Result.success(loginResponse);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = authService.refreshToken(request);
        return Result.success(loginResponse);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserVO userVO = authService.getCurrentUser(userId);
        return Result.success(userVO);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/controller/AuthController.java
git commit -m "feat(user): implement AuthController with register, login, refresh, me"
```

---

### Task 9: 创建 UserService

**Files:**
- Create: `src/main/java/org/sdb/aiban/service/UserService.java`

- [ ] **Step 1: 创建 UserService.java**

```java
package org.sdb.aiban.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sdb.aiban.common.exception.BusinessException;
import org.sdb.aiban.common.result.ResultCode;
import org.sdb.aiban.common.validator.UserValidator;
import org.sdb.aiban.dto.request.ChangePasswordRequest;
import org.sdb.aiban.dto.request.UpdateProfileRequest;
import org.sdb.aiban.dto.response.FileUploadResponse;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.entity.SysUser;
import org.sdb.aiban.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新非空字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userMapper.updateById(user);

        return buildUserResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 校验新密码强度
        userValidator.validatePassword(request.getNewPassword());

        // 校验新密码与原密码不同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.SAME_PASSWORD);
        }

        // 校验两次密码一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_MISMATCH);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("用户修改密码成功: {}", user.getUsername());
    }

    public FileUploadResponse uploadAvatar(Long userId, MultipartFile file) {
        // 校验文件
        userValidator.validateAvatar(file);

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        try {
            // 生成存储路径
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String fileName = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());
            String relativePath = "uploads/avatars/" + userId + "/" + datePath + "/" + fileName;

            // 创建目录
            Path basePath = Paths.get("./uploads/avatars/" + userId + "/" + datePath);
            Files.createDirectories(basePath);

            // 保存文件
            Path filePath = basePath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 更新用户头像
            user.setAvatar("/" + relativePath);
            userMapper.updateById(user);

            log.info("用户上传头像成功: {}", user.getUsername());

            return FileUploadResponse.builder()
                    .url("/" + relativePath)
                    .filename(fileName)
                    .build();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex > 0 ? filename.substring(dotIndex) : ".jpg";
    }

    private UserResponse buildUserResponse(SysUser user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(maskPhone(user.getPhone()))
                .role(user.getRole())
                .bio(user.getBio())
                .createTime(user.getCreateTime())
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/service/UserService.java
git commit -m "feat(user): implement UserService with updateProfile, changePassword, uploadAvatar"
```

---

### Task 10: 创建 UserController

**Files:**
- Create: `src/main/java/org/sdb/aiban/controller/UserController.java`

- [ ] **Step 1: 创建 UserController.java**

```java
package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.request.ChangePasswordRequest;
import org.sdb.aiban.dto.request.UpdateProfileRequest;
import org.sdb.aiban.dto.response.FileUploadResponse;
import org.sdb.aiban.dto.response.UserResponse;
import org.sdb.aiban.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户管理", description = "个人资料、密码修改、头像上传")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse userResponse = userService.updateProfile(userId, request);
        return Result.success(userResponse);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, request);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<FileUploadResponse> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        Long userId = (Long) authentication.getPrincipal();
        FileUploadResponse response = userService.uploadAvatar(userId, file);
        return Result.success(response);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/controller/UserController.java
git commit -m "feat(user): implement UserController with profile, password, avatar"
```

---

### Task 11: 更新 SecurityConfig 放行路径

**Files:**
- Modify: `src/main/java/org/sdb/aiban/security/SecurityConfig.java`

- [ ] **Step 1: 更新放行路径**

在 `SecurityConfig.java` 中，`requestMatchers` 添加 `/api/user/avatar`（头像上传接口，因为前端可能需要跨域上传）：

```java
// 在 .requestMatchers("/api/test/public").permitAll() 后添加
.requestMatchers(HttpMethod.POST, "/api/user/avatar").permitAll()
```

**注意：** 实际上头像上传需要认证，这里不需要放行。保持原样即可，无需修改。

- [ ] **Step 2: 确认无需修改**

跳过此 Task，SecurityConfig 已正确配置。

---

### Task 12: 修改 UserDetailsServiceImpl 使用数据库

**Files:**
- Modify: `src/main/java/org/sdb/aiban/security/UserDetailsServiceImpl.java`

- [ ] **Step 1: 更新 UserDetailsServiceImpl**

```java
package org.sdb.aiban.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.sdb.aiban.entity.SysUser;
import org.sdb.aiban.mapper.UserMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }

    @Data
    @AllArgsConstructor
    public static class UserPrincipal implements UserDetails {
        private Long userId;
        private String username;
        private String password;
        private String role;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/security/UserDetailsServiceImpl.java
git commit -m "feat(user): update UserDetailsServiceImpl to query database"
```

---

### Task 13: 启动验证

**Files:**
- 无

- [ ] **Step 1: 启动应用**

Run: `mvn spring-boot:run`
Expected: 启动成功，看到 `Started AibanApplication`

- [ ] **Step 2: 测试注册接口**

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234",
    "confirmPassword": "Test1234",
    "nickname": "测试用户"
  }'
```

Expected:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 3,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": null,
    "role": "STUDENT"
  }
}
```

- [ ] **Step 3: 测试登录接口**

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

Expected: 返回 accessToken、refreshToken、user 信息

- [ ] **Step 4: 测试获取当前用户**

使用登录返回的 accessToken：

```bash
curl http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer {accessToken}"
```

Expected: 返回用户信息

- [ ] **Step 5: 测试更新个人资料**

```bash
curl -X PUT http://localhost:8081/api/user/profile \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "新昵称",
    "bio": "这是我的个人简介"
  }'
```

Expected: 返回更新后的用户信息

- [ ] **Step 6: 测试修改密码**

```bash
curl -X PUT http://localhost:8081/api/user/password \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "123456",
    "newPassword": "NewPass123",
  "confirmPassword": "NewPass123"
}'
```

Expected: 返回成功

- [ ] **Step 7: 停止应用并提交**

```bash
taskkill //F //IM java.exe
git add -A
git commit -m "feat(user): complete user module with all 7 endpoints"
```

---

## Self-Review Checklist

- [ ] Spec coverage: 注册 ✅, 登录 ✅, Token刷新 ✅, 获取当前用户 ✅, 更新资料 ✅, 修改密码 ✅, 上传头像 ✅
- [ ] Placeholder scan: 无 TBD/TODO
- [ ] Type consistency: Request/Response DTO 与 Service/Controller 一致
- [ ] File structure: 与设计文档一致
