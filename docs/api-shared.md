# 共享接口文档 — 用户认证与用户管理

> 本文档包含鸿蒙客户端和 Web 管理端**共用**的接口。
> 基础路径：`/api`

---

## 统一响应格式

所有接口返回统一结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务端异常 |

分页接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ ... ],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

---

## 1. 认证模块 `/api/auth`

### 1.1 注册

```
POST /api/auth/register
```

**请求体：**

```json
{
  "username": "zhangsan",
  "password": "Abc12345",
  "confirmPassword": "Abc12345",
  "nickname": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 唯一用户名，4-20位字母数字下划线 |
| password | String | 是 | 6-20位，至少含字母和数字 |
| confirmPassword | String | 是 | 必须与 password 一致 |
| nickname | String | 否 | 昵称，不超过20字 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |

**响应：**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "role": "STUDENT"
  }
}
```

**错误场景：**

| 场景 | code | message |
|------|------|---------|
| 用户名已存在 | 400 | 用户名已被注册 |
| 两次密码不一致 | 400 | 两次输入的密码不一致 |
| 密码不符合规则 | 400 | 密码需包含字母和数字，长度6-20位 |

---

### 1.2 登录

```
POST /api/auth/login
```

**请求体：**

```json
{
  "username": "zhangsan",
  "password": "Abc12345"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "user": {
      "userId": 1,
      "username": "zhangsan",
      "nickname": "张三",
      "avatar": "https://...",
      "role": "STUDENT"
    }
  }
}
```

| 字段 | 说明 |
|------|------|
| accessToken | JWT 访问令牌，有效期 2 小时 |
| refreshToken | 刷新令牌，有效期 7 天 |
| expiresIn | access token 过期时间（秒） |
| role | STUDENT / ADMIN |

**错误场景：**

| 场景 | code | message |
|------|------|---------|
| 用户名或密码错误 | 400 | 用户名或密码错误 |

---

### 1.3 刷新 Token

```
POST /api/auth/refresh
```

**请求体：**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200
  }
}
```

---

### 1.4 获取当前用户信息

```
GET /api/auth/me
```

**请求头：** `Authorization: Bearer {accessToken}`

**响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "avatar": "https://...",
    "email": "zhangsan@example.com",
    "phone": "138****8000",
    "role": "STUDENT",
    "createTime": "2026-06-20T10:00:00"
  }
}
```

---

## 2. 用户模块 `/api/user`

### 2.1 更新个人资料（学生 + 管理员）

```
PUT /api/user/profile
```

**请求头：** `Authorization: Bearer {accessToken}`

**请求体：**

```json
{
  "nickname": "张三三",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "avatar": "https://...",
  "bio": "计算机科学与技术专业大三学生"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | 否 | 昵称 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| avatar | String | 否 | 头像 URL |
| bio | String | 否 | 个人简介，不超过200字 |

**响应：**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "userId": 1,
    "nickname": "张三三",
    "email": "newemail@example.com",
    "phone": "139****9000",
    "avatar": "https://...",
    "bio": "计算机科学与技术专业大三学生"
  }
}
```

---

### 2.2 修改密码

```
PUT /api/user/password
```

**请求头：** `Authorization: Bearer {accessToken}`

**请求体：**

```json
{
  "oldPassword": "Abc12345",
  "newPassword": "Xyz78901",
  "confirmPassword": "Xyz78901"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "密码修改成功"
}
```

**错误场景：**

| 场景 | code | message |
|------|------|---------|
| 原密码错误 | 400 | 原密码不正确 |
| 新密码与确认密码不一致 | 400 | 两次输入的密码不一致 |

---

### 2.3 获取用户详情（管理员）

```
GET /api/user/{userId}
```

**请求头：** `Authorization: Bearer {accessToken}` （需 ADMIN 角色）

**响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "avatar": "https://...",
    "email": "zhangsan@example.com",
    "phone": "138****8000",
    "role": "STUDENT",
    "status": "ACTIVE",
    "createTime": "2026-06-20T10:00:00",
    "lastLoginTime": "2026-06-22T14:30:00"
  }
}
```

---

### 2.4 用户列表（管理员）

```
GET /api/user/list?page=1&size=10&keyword=&role=&status=
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10 |
| keyword | String | 否 | 搜索关键词（用户名/昵称） |
| role | String | 否 | 角色筛选：STUDENT / ADMIN |
| status | String | 否 | 状态筛选：ACTIVE / DISABLED |

**响应：**（分页结构，见统一响应格式）

---

### 2.5 修改用户角色（管理员）

```
PUT /api/user/{userId}/role
```

**请求头：** `Authorization: Bearer {accessToken}` （需 ADMIN 角色）

**请求体：**

```json
{
  "role": "ADMIN"
}
```

---

### 2.6 禁用/启用用户（管理员）

```
PUT /api/user/{userId}/status
```

**请求头：** `Authorization: Bearer {accessToken}` （需 ADMIN 角色）

**请求体：**

```json
{
  "status": "DISABLED"
}
```

---

## 3. 文件上传 `/api/file`

### 3.1 上传头像

```
POST /api/file/avatar
```

**请求头：** `Authorization: Bearer {accessToken}`

**请求体：** `multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 图片文件，支持 jpg/png/gif，最大 5MB |

**响应：**

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://your-oss.com/avatars/2026/06/abc123.jpg",
    "filename": "abc123.jpg"
  }
}
```

---

### 3.2 上传简历文件

```
POST /api/file/resume
```

**请求头：** `Authorization: Bearer {accessToken}`

**请求体：** `multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 支持 pdf/doc/docx，最大 10MB |

**响应：**

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://your-oss.com/resumes/2026/06/abc123.pdf",
    "filename": "abc123.pdf",
    "size": 204800
  }
}
```

---

## 数据字典

### 用户角色 role

| 值 | 说明 |
|----|------|
| STUDENT | 学生（默认） |
| ADMIN | 管理员 |

### 用户状态 status

| 值 | 说明 |
|----|------|
| ACTIVE | 正常（默认） |
| DISABLED | 已禁用 |
