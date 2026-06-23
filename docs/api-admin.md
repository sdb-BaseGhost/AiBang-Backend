# Web 管理端接口文档 — 管理员后台

> 本文档包含 Vue3 管理后台调用的管理员接口。
> 基础路径：`/api`
> 认证方式：`Authorization: Bearer {accessToken}`（需 ADMIN 角色）

---

## 目录

1. 管理员仪表盘
2. 用户管理
3. 技能树管理
4. 系统配置

> 用户认证、个人资料管理等通用接口见 [api-shared.md](./api-shared.md)
> AI 辅导、简历、学习数据等学生端接口见 [api-mobile.md](./api-mobile.md)

---

## 1. 管理员仪表盘 `/api/admin`

### 1.1 获取仪表盘统计数据

```
GET /api/admin/dashboard
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userStats": {
      "totalUsers": 150,
      "todayNewUsers": 5,
      "activeUsers": 89,
      "onlineUsers": 12
    },
    "learningStats": {
      "totalStudyHours": 12500,
      "todayStudyHours": 320,
      "avgDailyHours": 2.8,
      "totalCheckins": 3200
    },
    "aiStats": {
      "totalChats": 850,
      "todayChats": 45,
      "avgSessionLength": 8.5,
      "resumeAnalyzed": 120
    },
    "skillStats": {
      "totalSkills": 30,
      "avgCompletionRate": 35.2,
      "topSkills": [
        { "skillName": "Java 基础", "completionRate": 78.5 },
        { "skillName": "HTML/CSS", "completionRate": 65.2 },
        { "skillName": "MySQL", "completionRate": 52.1 }
      ]
    },
    "weeklyTrend": [
      { "date": "2026-06-16", "users": 42, "chats": 38, "hours": 280 },
      { "date": "2026-06-17", "users": 45, "chats": 42, "hours": 310 },
      { "date": "2026-06-18", "users": 38, "chats": 35, "hours": 260 },
      { "date": "2026-06-19", "users": 50, "chats": 48, "hours": 350 },
      { "date": "2026-06-20", "users": 44, "chats": 40, "hours": 300 },
      { "date": "2026-06-21", "users": 48, "chats": 45, "hours": 330 },
      { "date": "2026-06-22", "users": 40, "chats": 36, "hours": 290 }
    ]
  }
}
```

---

### 1.2 获取用户增长趋势

```
GET /api/admin/user-trend?startDate=2026-06-01&endDate=2026-06-22
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "date": "2026-06-01", "newUsers": 3, "totalUsers": 120 },
    { "date": "2026-06-02", "newUsers": 5, "totalUsers": 125 },
    { "date": "2026-06-03", "newUsers": 2, "totalUsers": 127 }
  ]
}
```

---

### 1.3 获取学习活跃度排行

```
GET /api/admin/learning-ranking?page=1&size=10
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "userId": 1,
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": "https://...",
        "totalStudyHours": 85.5,
        "completedSkills": 12,
        "currentStreak": 15,
        "rank": 1
      },
      {
        "userId": 5,
        "username": "lisi",
        "nickname": "李四",
        "avatar": "https://...",
        "totalStudyHours": 72.0,
        "completedSkills": 10,
        "currentStreak": 12,
        "rank": 2
      }
    ],
    "total": 150,
    "current": 1,
    "size": 10,
    "pages": 15
  }
}
```

---

## 2. 用户管理 `/api/admin/user`

### 2.1 获取用户列表（管理版）

```
GET /api/admin/user/list?page=1&size=10&keyword=&role=&status=&sortBy=createTime&sortOrder=desc
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10 |
| keyword | String | 否 | 搜索关键词（用户名/昵称/邮箱） |
| role | String | 否 | 角色筛选：STUDENT / ADMIN |
| status | String | 否 | 状态筛选：ACTIVE / DISABLED |
| sortBy | String | 否 | 排序字段：createTime / lastLoginTime / username |
| sortOrder | String | 否 | 排序方式：asc / desc |

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "userId": 1,
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": "https://...",
        "email": "zhangsan@example.com",
        "phone": "138****8000",
        "role": "STUDENT",
        "status": "ACTIVE",
        "totalStudyHours": 85.5,
        "completedSkills": 12,
        "createTime": "2026-06-20T10:00:00",
        "lastLoginTime": "2026-06-22T14:30:00"
      }
    ],
    "total": 150,
    "current": 1,
    "size": 10,
    "pages": 15
  }
}
```

---

### 2.2 获取用户详情

```
GET /api/admin/user/{userId}
```

响应：

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
    "bio": "计算机科学与技术专业大三学生",
    "createTime": "2026-06-20T10:00:00",
    "lastLoginTime": "2026-06-22T14:30:00",
    "learningStats": {
      "totalStudyHours": 85.5,
      "completedSkills": 12,
      "inProgressSkills": 5,
      "currentStreak": 15,
      "resumeScore": 78,
      "chatCount": 25
    },
    "recentActivities": [
      {
        "type": "SKILL_STUDY",
        "title": "学习了 Java 集合框架",
        "createTime": "2026-06-22T14:00:00"
      },
      {
        "type": "AI_CHAT",
        "title": "AI 辅导：二叉树遍历",
        "createTime": "2026-06-22T13:30:00"
      }
    ]
  }
}
```

---

### 2.3 修改用户角色

```
PUT /api/admin/user/{userId}/role
```

请求体：

```json
{
  "role": "ADMIN"
}
```

响应：

```json
{
  "code": 200,
  "message": "角色修改成功"
}
```

---

### 2.4 禁用/启用用户

```
PUT /api/admin/user/{userId}/status
```

请求体：

```json
{
  "status": "DISABLED",
  "reason": "违反平台规定"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | ACTIVE / DISABLED |
| reason | String | 否 | 操作原因（禁用时建议填写） |

响应：

```json
{
  "code": 200,
  "message": "用户状态已更新"
}
```

---

### 2.5 重置用户密码

```
POST /api/admin/user/{userId}/reset-password
```

响应：

```json
{
  "code": 200,
  "message": "密码已重置",
  "data": {
    "tempPassword": "Abc123456"
  }
}
```

> 重置后用户需使用临时密码登录并修改密码。

---

### 2.6 批量导入用户

```
POST /api/admin/user/import
```

请求体：`multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel 文件（.xlsx），包含 username、password、nickname 列 |

响应：

```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "total": 50,
    "success": 48,
    "failed": 2,
    "failures": [
      { "row": 12, "username": "wangwu", "reason": "用户名已存在" },
      { "row": 35, "username": "zhaoliu", "reason": "密码不符合规则" }
    ]
  }
}
```

---

### 2.7 导出用户数据

```
GET /api/admin/user/export?role=&status=
```

响应：返回 Excel 文件流

---

## 3. 技能树管理 `/api/admin/skill`

### 3.1 获取技能分类列表（管理版）

```
GET /api/admin/skill/categories
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryId": 1,
      "name": "编程语言",
      "icon": "code",
      "sortOrder": 1,
      "skillCount": 8,
      "totalUsers": 120,
      "avgCompletionRate": 45.2
    }
  ]
}
```

---

### 3.2 创建技能分类

```
POST /api/admin/skill/category
```

请求体：

```json
{
  "name": "云计算",
  "icon": "cloud",
  "sortOrder": 6,
  "description": "云原生与云计算相关技能"
}
```

响应：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "categoryId": 6,
    "name": "云计算",
    "icon": "cloud",
    "sortOrder": 6
  }
}
```

---

### 3.3 更新技能分类

```
PUT /api/admin/skill/category/{categoryId}
```

请求体：

```json
{
  "name": "云计算与云原生",
  "icon": "cloud",
  "sortOrder": 6,
  "description": "云原生、容器化与云计算相关技能"
}
```

---

### 3.4 删除技能分类

```
DELETE /api/admin/skill/category/{categoryId}
```

> 如果分类下有技能，需先移除或迁移技能后才能删除。

---

### 3.5 获取技能列表（管理版）

```
GET /api/admin/skill/list?categoryId=&keyword=&level=&page=1&size=10
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "skillId": 1,
        "name": "Java",
        "categoryId": 1,
        "categoryName": "编程语言",
        "level": "BEGINNER",
        "levelLabel": "入门",
        "description": "Java 编程语言核心技能",
        "userCount": 85,
        "completionRate": 62.5,
        "createTime": "2026-06-20T10:00:00"
      }
    ],
    "total": 30,
    "current": 1,
    "size": 10,
    "pages": 3
  }
}
```

---

### 3.6 创建技能

```
POST /api/admin/skill
```

请求体：

```json
{
  "name": "Docker 容器化",
  "categoryId": 6,
  "level": "INTERMEDIATE",
  "description": "Docker 容器化部署与管理",
  "sortOrder": 1,
  "parentSkillId": null
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 技能名称 |
| categoryId | Long | 是 | 所属分类 ID |
| level | String | 是 | BEGINNER / INTERMEDIATE / ADVANCED / EXPERT |
| description | String | 否 | 技能描述 |
| sortOrder | Integer | 否 | 排序，越大越靠前 |
| parentSkillId | Long | 否 | 父技能 ID（用于构建技能树层级） |

---

### 3.7 更新技能

```
PUT /api/admin/skill/{skillId}
```

---

### 3.8 删除技能

```
DELETE /api/admin/skill/{skillId}
```

---

### 3.9 获取技能统计

```
GET /api/admin/skill/stats
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCategories": 5,
    "totalSkills": 30,
    "avgCompletionRate": 35.2,
    "topCompletedSkills": [
      { "skillName": "Java 基础", "completionRate": 78.5, "userCount": 85 },
      { "skillName": "HTML/CSS", "completionRate": 65.2, "userCount": 72 }
    ],
    "leastCompletedSkills": [
      { "skillName": "微服务架构", "completionRate": 8.2, "userCount": 15 },
      { "skillName": "Kubernetes", "completionRate": 5.1, "userCount": 8 }
    ],
    "categoryStats": [
      { "categoryName": "编程语言", "skillCount": 8, "avgCompletion": 45.2 },
      { "categoryName": "前端开发", "skillCount": 6, "avgCompletion": 38.5 },
      { "categoryName": "后端开发", "skillCount": 7, "avgCompletion": 28.3 },
      { "categoryName": "数据库", "skillCount": 5, "avgCompletion": 42.0 },
      { "categoryName": "AI/机器学习", "skillCount": 4, "avgCompletion": 15.5 }
    ]
  }
}
```

---

## 4. 系统配置 `/api/admin/config`

### 4.1 获取系统配置

```
GET /api/admin/config
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "ai": {
      "difyApiKey": "app-****",
      "difyBaseUrl": "https://api.dify.ai/v1",
      "chatflowId": "chatflow-xxx",
      "workflowId": "workflow-xxx",
      "maxTokens": 4096,
      "temperature": 0.7
    },
    "upload": {
      "maxAvatarSize": 5242880,
      "maxResumeSize": 10485760,
      "allowedResumeTypes": ["pdf", "doc", "docx"],
      "allowedAvatarTypes": ["jpg", "png", "gif"]
    },
    "system": {
      "siteName": "AI伴学平台",
      "maintenanceMode": false,
      "registrationEnabled": true
    }
  }
}
```

---

### 4.2 更新系统配置

```
PUT /api/admin/config
```

请求体：

```json
{
  "ai": {
    "maxTokens": 4096,
    "temperature": 0.7
  },
  "system": {
    "siteName": "AI伴学平台",
    "maintenanceMode": false,
    "registrationEnabled": true
  }
}
```

---

## 5. 日志与监控 `/api/admin/logs`

### 5.1 获取操作日志

```
GET /api/admin/logs/operation?page=1&size=20&userId=&action=&startDate=&endDate=
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "logId": 1,
        "userId": 100,
        "username": "admin",
        "action": "UPDATE_USER_ROLE",
        "target": "用户 zhangsan",
        "detail": "将角色从 STUDENT 改为 ADMIN",
        "ip": "192.168.1.100",
        "createTime": "2026-06-22T14:00:00"
      }
    ],
    "total": 500,
    "current": 1,
    "size": 20,
    "pages": 25
  }
}
```

---

### 5.2 获取 AI 调用日志

```
GET /api/admin/logs/ai?page=1&size=20&type=&startDate=&endDate=
```

| 参数 | 类型 | 说明 |
|------|------|------|
| type | String | CHAT / RESUME / EVALUATE / CAREER |

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "logId": 1,
        "userId": 1,
        "username": "zhangsan",
        "type": "CHAT",
        "tokens": 1250,
        "duration": 3200,
        "status": "SUCCESS",
        "createTime": "2026-06-22T14:05:00"
      }
    ],
    "total": 850,
    "current": 1,
    "size": 20,
    "pages": 43
  }
}
```

---

## 接口总览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 仪表盘 | GET | /api/admin/dashboard | 统计数据 |
| 仪表盘 | GET | /api/admin/user-trend | 用户增长趋势 |
| 仪表盘 | GET | /api/admin/learning-ranking | 学习活跃度排行 |
| 用户管理 | GET | /api/admin/user/list | 用户列表（管理版） |
| 用户管理 | GET | /api/admin/user/{id} | 用户详情 |
| 用户管理 | PUT | /api/admin/user/{id}/role | 修改角色 |
| 用户管理 | PUT | /api/admin/user/{id}/status | 禁用/启用 |
| 用户管理 | POST | /api/admin/user/{id}/reset-password | 重置密码 |
| 用户管理 | POST | /api/admin/user/import | 批量导入 |
| 用户管理 | GET | /api/admin/user/export | 导出数据 |
| 技能树 | GET | /api/admin/skill/categories | 分类列表（管理版） |
| 技能树 | POST | /api/admin/skill/category | 创建分类 |
| 技能树 | PUT | /api/admin/skill/category/{id} | 更新分类 |
| 技能树 | DELETE | /api/admin/skill/category/{id} | 删除分类 |
| 技能树 | GET | /api/admin/skill/list | 技能列表（管理版） |
| 技能树 | POST | /api/admin/skill | 创建技能 |
| 技能树 | PUT | /api/admin/skill/{id} | 更新技能 |
| 技能树 | DELETE | /api/admin/skill/{id} | 删除技能 |
| 技能树 | GET | /api/admin/skill/stats | 技能统计 |
| 系统配置 | GET | /api/admin/config | 获取配置 |
| 系统配置 | PUT | /api/admin/config | 更新配置 |
| 日志 | GET | /api/admin/logs/operation | 操作日志 |
| 日志 | GET | /api/admin/logs/ai | AI 调用日志 |
