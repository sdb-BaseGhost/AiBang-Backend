# 设计文档：后端基础框架搭建

> 日期：2026-06-23
> 范围：pom.xml 依赖 + 包结构 + 统一响应 + 异常处理 + MyBatis-Plus + JWT Security + application.yml

---

## 1. 目标

搭建 Spring Boot 3 后端项目的基础框架，为后续业务模块（用户体系、技能树、AI辅导等）提供统一的依赖、配置、响应格式、异常处理和认证鉴权能力。

## 2. 范围

| 序号 | 内容 | 状态 |
|------|------|------|
| 1 | 补充 pom.xml 缺失依赖，移除多余依赖 | ✅ |
| 2 | 标准包结构（controller/service/mapper/entity/config/common/security） | ✅ |
| 3 | 统一响应封装 Result\<T\> + PageResult\<T\> | ✅ |
| 4 | 全局异常处理器 GlobalExceptionHandler | ✅ |
| 5 | MyBatis-Plus 配置（分页插件、自动填充、逻辑删除） | ✅ |
| 6 | JWT 工具类 + Security 配置（简单版） | ✅ |
| 7 | application.yml 完整配置 | ✅ |
| 8 | 数据库建表 SQL | ❌ 不在本次范围 |
| 9 | CORS 跨域配置 | ✅ 包含在 Config 层 |
| 10 | Knife4j API 文档配置 | ✅ 依赖已加入，后续各模块添加注解 |

## 3. 技术选型决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| JWT/Security | 简单版 | 实训项目够用，密钥写在 yml，角色在 Service 层判断 |
| 文件存储 | 本地存储 | 无云服务依赖，uploads/ 目录映射虚拟路径 |
| MySQL | root/1234@localhost:3306 | 本地开发环境 |
| SQL Server | 移除 mssql-jdbc | 项目只用 MySQL |

## 4. 包结构

```
src/main/java/org/sdb/aiban/
├── AibanApplication.java
├── common/
│   ├── result/
│   │   ├── Result.java
│   │   ├── PageResult.java
│   │   └── ResultCode.java
│   └── exception/
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
├── config/
│   ├── MyBatisPlusConfig.java
│   ├── CorsConfig.java
│   └── WebMvcConfig.java
├── security/
│   ├── JwtUtils.java
│   ├── JwtAuthFilter.java
│   ├── UserDetailsServiceImpl.java
│   └── SecurityConfig.java
└── entity/
    └── BaseEntity.java
```

## 5. 详细设计

### 5.1 Common 层

#### Result\<T\>

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

#### PageResult\<T\>

```java
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

#### ResultCode

| 枚举值 | code | message |
|--------|------|---------|
| SUCCESS | 200 | success |
| BAD_REQUEST | 400 | 参数错误 |
| UNAUTHORIZED | 401 | 未登录或Token已过期 |
| FORBIDDEN | 403 | 无权限 |
| NOT_FOUND | 404 | 资源不存在 |
| INTERNAL_ERROR | 500 | 服务器内部异常 |
| USER_NOT_FOUND | 1001 | 用户不存在 |
| USERNAME_EXISTS | 1002 | 用户名已被注册 |
| PASSWORD_ERROR | 1003 | 用户名或密码错误 |

#### BusinessException

```java
@Getter
public class BusinessException extends RuntimeException {
    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
}
```

#### GlobalExceptionHandler

- `BusinessException` → 对应 ResultCode
- `MethodArgumentNotValidException` → 400 + 字段校验信息
- `AccessDeniedException` → 403
- `Exception` → 500 兜底

### 5.2 Config 层

#### MyBatisPlusConfig

- `@MapperScan("org.sdb.aiban.mapper")`
- 分页插件：`PaginationInnerInterceptor(DbType.MYSQL)`
- 自动填充：insert 填充 createTime + updateTime，update 填充 updateTime

#### CorsConfig

- 允许所有来源（开发阶段）
- 允许 GET/POST/PUT/DELETE
- 允许 Authorization 头
- 允许 credentials

#### WebMvcConfig

- `/uploads/**` → `file:./uploads/`

### 5.3 Security 层

#### JwtUtils

- 读取 `jwt.secret` / `jwt.access-token-expiration` / `jwt.refresh-token-expiration`
- `generateAccessToken(userId, username, role)` → Claims 包含 type=access
- `generateRefreshToken(userId, username)` → Claims 包含 type=refresh
- `parseToken(token)` → Claims，失败抛异常
- `getUserId/getUsername/getRole(Claims)` → 提取方法

#### JwtAuthFilter

- 继承 `OncePerRequestFilter`
- 从 Header 取 `Authorization: Bearer xxx`
- 解析 token → 构建 `UsernamePasswordAuthenticationToken` → 放入 SecurityContext
- 无 token 或解析失败 → 放行（不设认证信息）

#### UserDetailsServiceImpl

- 占位实现：返回固定 UserPrincipal
- 后续模块1（用户体系）中用 UserMapper 查询数据库

#### SecurityConfig

- 禁用 CSRF
- 禁用 session（JWT 无状态）
- 放行路径：`/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/uploads/**`, `/doc.html`, `/swagger-ui/**`, `/v3/api-docs/**`
- 其余路径需要认证
- JwtAuthFilter 加在 UsernamePasswordAuthenticationFilter 之前
- `PasswordEncoder` → `BCryptPasswordEncoder`

#### 请求流程

```
客户端请求 → JwtAuthFilter（解析token设认证信息）
  → SecurityFilterChain（放行路径直接通过 / 其余检查认证）
```

### 5.4 Entity 基类

```java
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

### 5.5 application.yml

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

jwt:
  secret: aiban-jwt-secret-key-2026-very-long-and-secure
  access-token-expiration: 7200000
  refresh-token-expiration: 604800000

file:
  upload-path: ./
```

### 5.6 pom.xml 变更

| 操作 | 依赖 | 版本 |
|------|------|------|
| 新增 | spring-boot-starter-security | 继承 parent |
| 新增 | mybatis-plus-spring-boot3-starter | 3.5.7 |
| 新增 | jjwt-api / jjwt-impl / jjwt-jackson | 0.12.6 |
| 新增 | knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 |
| 新增 | lombok | 继承 parent |
| 新增 | spring-boot-starter-validation | 继承 parent |
| 替换 | spring-boot-starter-web（替代 webmvc） | 继承 parent |
| 替换 | spring-boot-starter-test（替代 webmvc-test） | 继承 parent |
| 移除 | mssql-jdbc | - |
| 保留 | spring-boot-devtools | runtime |
| 保留 | mysql-connector-j | runtime |

## 6. 不做什么

- 不实现业务模块（用户注册登录、技能树等后续单独做）
- 不建数据库表（后续各模块各自建表）
- 不做细粒度权限控制（角色校验在 Service 层手动判断）
- 不做 Refresh Token 的数据库存储（当前先用内存验证）

## 7. 验收标准

| 验收项 | 正例 | 反例 |
|--------|------|------|
| 项目启动 | `mvn spring-boot:run` 成功启动，无报错 | 数据库连接失败时有明确错误提示 |
| 统一响应 | `Result.success(data)` → `{"code":200,"message":"success","data":...}` | - |
| 全局异常 | 抛出 `BusinessException(UNAUTHORIZED)` → 返回 401 | 未捕获异常 → 返回 500 + 通用信息 |
| JWT 生成 | 调用 `generateAccessToken()` 返回有效 token | 过期 token → parseToken 抛异常 |
| Security | 无 token 访问 `/api/auth/me` → 401 | 带 token 访问 → 正常放行 |
| 放行路径 | 无 token 访问 `/api/auth/login` → 正常通过 | - |
| MyBatis-Plus | 分页查询返回 PageResult 结构 | - |
| CORS | 前端跨域请求 → 正常返回 | - |

## 8. 风险

| 风险 | 缓解措施 |
|------|----------|
| 数据库不存在导致启动失败 | application.yml 中明确数据库名，README 说明需先建库 |
| JWT 密钥泄露 | 仅限开发阶段硬编码，后续可迁移至环境变量 |
| Knife4j 与 Security 冲突 | 放行路径已包含 Knife4j 相关路径 |
