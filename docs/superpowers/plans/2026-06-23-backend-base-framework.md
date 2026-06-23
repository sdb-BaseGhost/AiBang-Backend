# 后端基础框架 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建 Spring Boot 3 后端项目的基础框架，包含依赖管理、统一响应、异常处理、MyBatis-Plus、JWT 认证和完整配置。

**Architecture:** 分层搭建，从底层依赖到上层安全配置逐层推进。每层只依赖下层，确保编译通过和项目可启动。

**Tech Stack:** Spring Boot 3.4.7 · Spring Security · MyBatis-Plus 3.5.7 · JWT (jjwt 0.12.6) · Knife4j 4.5.0 · MySQL 8.0 · Lombok

---

## File Structure

```
pom.xml                                    # MODIFY - 更新依赖
src/main/resources/application.yml          # CREATE - 替代 application.properties
src/main/resources/application.properties   # DELETE - 被 yml 替代
src/main/java/org/sdb/aiban/
├── common/
│   ├── result/
│   │   ├── Result.java                    # CREATE
│   │   ├── PageResult.java                # CREATE
│   │   └── ResultCode.java                # CREATE
│   └── exception/
│       ├── BusinessException.java          # CREATE
│       └── GlobalExceptionHandler.java     # CREATE
├── config/
│   ├── MyBatisPlusConfig.java              # CREATE
│   ├── CorsConfig.java                     # CREATE
│   └── WebMvcConfig.java                   # CREATE
├── security/
│   ├── JwtUtils.java                       # CREATE
│   ├── JwtAuthFilter.java                  # CREATE
│   ├── UserDetailsServiceImpl.java          # CREATE
│   └── SecurityConfig.java                 # CREATE
└── entity/
    └── BaseEntity.java                     # CREATE
```

---

### Task 1: 更新 pom.xml 依赖

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 更新 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.7</version>
        <relativePath/>
    </parent>
    <groupId>org.sdb</groupId>
    <artifactId>AIBAN</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>AIBAN</name>
    <description>AI伴学与职业成长平台</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <jjwt.version>0.12.6</jjwt.version>
        <knife4j.version>4.5.0</knife4j.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Knife4j API 文档 -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 验证依赖下载**

Run: `mvn dependency:resolve -q`
Expected: 无错误输出

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "chore: update pom.xml dependencies - add security, mybatis-plus, jwt, knife4j, remove mssql"
```

---

### Task 2: 创建 application.yml 配置

**Files:**
- Create: `src/main/resources/application.yml`
- Delete: `src/main/resources/application.properties`

- [ ] **Step 1: 创建 application.yml**

```yaml
spring:
  application:
    name: AIBAN
  datasource:
    url: jdbc:mysql://localhost:3306/aiban?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: root
    password: "1234"
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      id-type: auto
  mapper-locations: classpath*:/mapper/**/*.xml

jwt:
  secret: aiban-jwt-secret-key-2026-very-long-and-secure
  access-token-expiration: 7200000
  refresh-token-expiration: 604800000

file:
  upload-path: ./

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true

knife4j:
  enable: true
  setting:
    language: zh_cn
```

- [ ] **Step 2: 删除 application.properties**

Run: `del src\main\resources\application.properties`

- [ ] **Step 3: 创建 mapper 目录**

Run: `mkdir src\main\resources\mapper`

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/
git commit -m "feat: add application.yml with MySQL, JWT, MyBatis-Plus, Knife4j config"
```

---

### Task 3: 创建 Common 层 - ResultCode 和 Result

**Files:**
- Create: `src/main/java/org/sdb/aiban/common/result/ResultCode.java`
- Create: `src/main/java/org/sdb/aiban/common/result/Result.java`

- [ ] **Step 1: 创建 ResultCode.java**

```java
package org.sdb.aiban.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部异常"),

    // 用户模块 1001-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已被注册"),
    PASSWORD_ERROR(1003, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用");

    private final int code;
    private final String message;
}
```

- [ ] **Step 2: 创建 Result.java**

```java
package org.sdb.aiban.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/sdb/aiban/common/result/
git commit -m "feat: add Result and ResultCode for unified response"
```

---

### Task 4: 创建 Common 层 - PageResult

**Files:**
- Create: `src/main/java/org/sdb/aiban/common/result/PageResult.java`

- [ ] **Step 1: 创建 PageResult.java**

```java
package org.sdb.aiban.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public static <T> PageResult<T> from(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/common/result/PageResult.java
git commit -m "feat: add PageResult for paginated response"
```

---

### Task 5: 创建 Common 层 - 异常处理

**Files:**
- Create: `src/main/java/org/sdb/aiban/common/exception/BusinessException.java`
- Create: `src/main/java/org/sdb/aiban/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 BusinessException.java**

```java
package org.sdb.aiban.common.exception;

import lombok.Getter;
import org.sdb.aiban.common.result.ResultCode;

@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
```

- [ ] **Step 2: 创建 GlobalExceptionHandler.java**

```java
package org.sdb.aiban.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.common.result.ResultCode;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.error(e.getResultCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validation failed: {}", errors);
        Result<Map<String, String>> result = new Result<>(400, "参数错误", errors);
        return result;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/sdb/aiban/common/exception/
git commit -m "feat: add BusinessException and GlobalExceptionHandler"
```

---

### Task 6: 创建 Config 层 - MyBatisPlusConfig

**Files:**
- Create: `src/main/java/org/sdb/aiban/config/MyBatisPlusConfig.java`

- [ ] **Step 1: 创建 MyBatisPlusConfig.java**

```java
package org.sdb.aiban.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@MapperScan("org.sdb.aiban.mapper")
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/config/MyBatisPlusConfig.java
git commit -m "feat: add MyBatisPlusConfig with pagination and auto-fill"
```

---

### Task 7: 创建 Config 层 - CORS 和 WebMvc

**Files:**
- Create: `src/main/java/org/sdb/aiban/config/CorsConfig.java`
- Create: `src/main/java/org/sdb/aiban/config/WebMvcConfig.java`

- [ ] **Step 1: 创建 CorsConfig.java**

```java
package org.sdb.aiban.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 2: 创建 WebMvcConfig.java**

```java
package org.sdb.aiban.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:./}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "uploads/");
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/sdb/aiban/config/CorsConfig.java src/main/java/org/sdb/aiban/config/WebMvcConfig.java
git commit -m "feat: add CorsConfig and WebMvcConfig for CORS and file upload"
```

---

### Task 8: 创建 Entity 基类

**Files:**
- Create: `src/main/java/org/sdb/aiban/entity/BaseEntity.java`

- [ ] **Step 1: 创建 BaseEntity.java**

```java
package org.sdb.aiban.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/entity/BaseEntity.java
git commit -m "feat: add BaseEntity with id, timestamps and logical delete"
```

---

### Task 9: 创建 Security 层 - JwtUtils

**Files:**
- Create: `src/main/java/org/sdb/aiban/security/JwtUtils.java`

- [ ] **Step 1: 创建 JwtUtils.java**

```java
package org.sdb.aiban.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", "access");
        return createToken(claims, accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        return createToken(claims, refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public String getTokenType(Claims claims) {
        return claims.get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired");
            return false;
        } catch (Exception e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/security/JwtUtils.java
git commit -m "feat: add JwtUtils for token generation and validation"
```

---

### Task 10: 创建 Security 层 - JwtAuthFilter

**Files:**
- Create: `src/main/java/org/sdb/aiban/security/JwtAuthFilter.java`

- [ ] **Step 1: 创建 JwtAuthFilter.java**

```java
package org.sdb.aiban.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            try {
                Claims claims = jwtUtils.parseToken(token);
                String type = jwtUtils.getTokenType(claims);

                if ("access".equals(type)) {
                    Long userId = jwtUtils.getUserId(claims);
                    String username = jwtUtils.getUsername(claims);
                    String role = jwtUtils.getRole(claims);

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.warn("Failed to set authentication: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/security/JwtAuthFilter.java
git commit -m "feat: add JwtAuthFilter for request authentication"
```

---

### Task 11: 创建 Security 层 - UserDetailsServiceImpl（占位）

**Files:**
- Create: `src/main/java/org/sdb/aiban/security/UserDetailsServiceImpl.java`

- [ ] **Step 1: 创建 UserDetailsServiceImpl.java**

```java
package org.sdb.aiban.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 占位实现：返回固定用户
        // 后续模块1（用户体系）中用 UserMapper 查询数据库
        if ("admin".equals(username)) {
            return new UserPrincipal(1L, "admin", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi", "ADMIN");
        }
        if ("student".equals(username)) {
            return new UserPrincipal(2L, "student", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi", "STUDENT");
        }
        throw new UsernameNotFoundException("用户不存在: " + username);
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

**说明：** 密码 `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi` 是 BCrypt 加密后的 "123456"，用于开发阶段测试。

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/security/UserDetailsServiceImpl.java
git commit -m "feat: add placeholder UserDetailsServiceImpl for development"
```

---

### Task 12: 创建 Security 层 - SecurityConfig

**Files:**
- Create: `src/main/java/org/sdb/aiban/security/SecurityConfig.java`

- [ ] **Step 1: 创建 SecurityConfig.java**

```java
package org.sdb.aiban.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 认证接口放行
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                // 静态资源放行
                .requestMatchers("/uploads/**").permitAll()
                // Knife4j 放行
                .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                // OPTIONS 请求放行（CORS 预检）
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 其余请求需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/security/SecurityConfig.java
git commit -m "feat: add SecurityConfig with JWT filter chain"
```

---

### Task 13: 创建临时 Controller 验证框架

**Files:**
- Create: `src/main/java/org/sdb/aiban/controller/TestController.java`

- [ ] **Step 1: 创建 TestController.java**

```java
package org.sdb.aiban.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sdb.aiban.common.result.Result;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public Result<Map<String, String>> publicEndpoint() {
        return Result.success(Map.of("message", "这是一个公开接口，无需登录"));
    }

    @GetMapping("/protected")
    public Result<Map<String, String>> protectedEndpoint() {
        return Result.success(Map.of("message", "这是一个需要登录的接口"));
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/sdb/aiban/controller/TestController.java
git commit -m "feat: add TestController for framework verification"
```

---

### Task 14: 启动验证

**Files:**
- Modify: 无

- [ ] **Step 1: 确保 MySQL 中存在 aiban 数据库**

Run: `mysql -u root -p1234 -e "CREATE DATABASE IF NOT EXISTS aiban DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"`

- [ ] **Step 2: 启动应用**

Run: `mvn spring-boot:run`
Expected: 启动成功，看到 `Started AibanApplication in X seconds`

**注意：** 如果看到 MyBatis-Plus 的日志输出和 SQL 打印，说明配置生效。

- [ ] **Step 3: 测试公开接口**

Run: `curl http://localhost:8080/api/test/public`
Expected:
```json
{"code":200,"message":"success","data":{"message":"这是一个公开接口，无需登录"}}
```

- [ ] **Step 4: 测试需要认证的接口（无 token）**

Run: `curl http://localhost:8080/api/test/protected`
Expected: 401 Unauthorized

- [ ] **Step 5: 测试登录获取 token（临时）**

由于尚未实现登录接口，可用 JwtUtils 直接生成 token 测试，或等待模块1实现。

- [ ] **Step 6: 测试 Knife4j 文档**

浏览器访问: `http://localhost:8080/doc.html`
Expected: Knife4j 文档页面正常加载

- [ ] **Step 7: 停止应用并最终提交**

```bash
git add -A
git commit -m "feat: complete backend base framework setup"
```

---

## Self-Review Checklist

- [ ] Spec coverage: pom.xml ✅, application.yml ✅, Result/PageResult/ResultCode ✅, BusinessException/GlobalExceptionHandler ✅, MyBatisPlusConfig ✅, CorsConfig ✅, WebMvcConfig ✅, JwtUtils ✅, JwtAuthFilter ✅, UserDetailsServiceImpl ✅, SecurityConfig ✅, BaseEntity ✅
- [ ] Placeholder scan: 无 TBD/TODO（UserDetailsServiceImpl 是明确的占位实现，有注释说明）
- [ ] Type consistency: JwtUtils 返回类型与 JwtAuthFilter 消费类型一致；Result/ResultCode 在所有异常处理中一致使用
