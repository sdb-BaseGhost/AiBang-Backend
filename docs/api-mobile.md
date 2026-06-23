# 鸿蒙客户端接口文档 — 学生用户端

> 本文档包含 HarmonyOS NEXT 鸿蒙客户端调用的接口。
> 基础路径：`/api`
> 认证方式：`Authorization: Bearer {accessToken}`

---

## 目录

1. AI 智能辅导（Dify Chatflow）
2. 技能树管理
3. 简历诊断与优化
4. 学习数据看板
5. 职业规划
6. 学习打卡

> 用户认证、个人资料管理等接口见 [api-shared.md](./api-shared.md)

---

## 1. AI 智能辅导 `/api/chat`

基于 Dify Chatflow 实现的多轮对话 AI 学业助手。

### 1.1 创建对话会话

```
POST /api/chat/session
```

请求体：

```json
{
  "title": "数据结构复习"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 否 | 会话标题，默认"新对话" |

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": 101,
    "title": "数据结构复习",
    "createTime": "2026-06-22T14:00:00"
  }
}
```

---

### 1.2 获取对话会话列表

```
GET /api/chat/sessions?page=1&size=10
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "sessionId": 101,
        "title": "数据结构复习",
        "lastMessage": "二叉树的遍历方式有...",
        "lastMessageTime": "2026-06-22T14:05:00",
        "messageCount": 6,
        "createTime": "2026-06-22T14:00:00"
      }
    ],
    "total": 5,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

---

### 1.3 获取对话消息列表

```
GET /api/chat/session/{sessionId}/messages?page=1&size=20
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "messageId": 1001,
        "role": "USER",
        "content": "二叉树的前序遍历是什么？",
        "createTime": "2026-06-22T14:01:00"
      },
      {
        "messageId": 1002,
        "role": "ASSISTANT",
        "content": "二叉树的前序遍历是指按照 **根节点 → 左子树 → 右子树** 的顺序访问...",
        "createTime": "2026-06-22T14:01:03"
      }
    ],
    "total": 6,
    "current": 1,
    "size": 20,
    "pages": 1
  }
}
```

| role 值 | 说明 |
|---------|------|
| USER | 用户消息 |
| ASSISTANT | AI 回复 |

---

### 1.4 发送消息（核心接口）

```
POST /api/chat/message
```

请求体：

```json
{
  "sessionId": 101,
  "content": "能举个具体的例子吗？"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | Long | 是 | 会话 ID |
| content | String | 是 | 用户消息内容，不超过 2000 字 |

响应（SSE 流式返回）：

```
Content-Type: text/event-stream

data: {"messageId":1003,"content":"当然","finished":false}
data: {"messageId":1003,"content":"！假设","finished":false}
data: {"messageId":1003,"content":"我们有一棵...","finished":false}
data: {"messageId":1003,"content":"","finished":true}
```

非流式响应（降级方案）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "messageId": 1003,
    "content": "当然！假设我们有一棵二叉树...",
    "createTime": "2026-06-22T14:02:00"
  }
}
```

> **对接说明**：后端调用 Dify Chatflow API，将 Dify 的流式响应透传给客户端。
> 鸿蒙端需要使用 HTTP 流式读取来接收 SSE 数据。
> 如鸿蒙端不支持 SSE，可降级为普通 HTTP 请求（等待完整响应返回）。

---

### 1.5 删除对话会话

```
DELETE /api/chat/session/{sessionId}
```

响应：

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 2. 技能树管理 `/api/skill`

### 2.1 获取技能分类列表

```
GET /api/skill/categories
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "categoryId": 1, "name": "编程语言", "icon": "code", "sortOrder": 1, "skillCount": 8 },
    { "categoryId": 2, "name": "前端开发", "icon": "web", "sortOrder": 2, "skillCount": 6 },
    { "categoryId": 3, "name": "后端开发", "icon": "server", "sortOrder": 3, "skillCount": 7 },
    { "categoryId": 4, "name": "数据库", "icon": "database", "sortOrder": 4, "skillCount": 5 },
    { "categoryId": 5, "name": "AI / 机器学习", "icon": "robot", "sortOrder": 5, "skillCount": 4 }
  ]
}
```

---

### 2.2 获取技能树（按分类）

```
GET /api/skill/tree?categoryId=1
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 否 | 分类 ID，不传返回全部 |

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "skillId": 1,
      "name": "Java",
      "categoryId": 1,
      "categoryName": "编程语言",
      "level": "BEGINNER",
      "levelLabel": "入门",
      "children": [
        { "skillId": 11, "name": "Java 基础语法", "level": "BEGINNER", "levelLabel": "入门", "children": [] },
        { "skillId": 12, "name": "Java 面向对象", "level": "BEGINNER", "levelLabel": "入门", "children": [] },
        { "skillId": 13, "name": "Java 集合框架", "level": "INTERMEDIATE", "levelLabel": "进阶", "children": [] }
      ]
    }
  ]
}
```

| level 值 | 说明 |
|----------|------|
| BEGINNER | 入门 |
| INTERMEDIATE | 进阶 |
| ADVANCED | 高级 |
| EXPERT | 专家 |

---

### 2.3 获取我的技能进度

```
GET /api/skill/user/progress
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalSkills": 30,
    "completedCount": 5,
    "inProgressCount": 8,
    "notStartedCount": 17,
    "overallProgress": 36.7,
    "skillProgressList": [
      {
        "skillId": 11,
        "skillName": "Java 基础语法",
        "categoryName": "编程语言",
        "status": "COMPLETED",
        "progress": 100,
        "rating": 4,
        "updateTime": "2026-06-20T10:00:00"
      },
      {
        "skillId": 12,
        "skillName": "Java 面向对象",
        "categoryName": "编程语言",
        "status": "IN_PROGRESS",
        "progress": 60,
        "rating": 0,
        "updateTime": "2026-06-22T14:00:00"
      }
    ]
  }
}
```

| status 值 | 说明 |
|-----------|------|
| NOT_STARTED | 未开始 |
| IN_PROGRESS | 进行中 |
| COMPLETED | 已完成 |

---

### 2.4 更新技能进度

```
PUT /api/skill/user/progress
```

请求体：

```json
{
  "skillId": 12,
  "progress": 80,
  "rating": 3,
  "note": "学习了继承和多态"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillId | Long | 是 | 技能 ID |
| progress | Integer | 是 | 进度百分比 0-100 |
| rating | Integer | 否 | 自评等级 1-5 |
| note | String | 否 | 学习笔记，不超过 500 字 |

响应：

```json
{
  "code": 200,
  "message": "进度更新成功",
  "data": {
    "skillId": 12,
    "skillName": "Java 面向对象",
    "progress": 80,
    "status": "IN_PROGRESS",
    "overallProgress": 40.0
  }
}
```

> 当 progress=100 时，status 自动变为 COMPLETED。

---

### 2.5 AI 技能评估

```
POST /api/skill/evaluate
```

请求体：

```json
{
  "skillId": 12,
  "answers": [
    { "questionId": 1, "answer": "多态是指同一个方法调用在不同对象上表现出不同行为" },
    { "questionId": 2, "answer": "使用 extends 关键字实现继承" }
  ]
}
```

响应：

```json
{
  "code": 200,
  "message": "评估完成",
  "data": {
    "skillId": 12,
    "skillName": "Java 面向对象",
    "score": 85,
    "level": "INTERMEDIATE",
    "levelLabel": "进阶",
    "feedback": "你对继承和多态有较好的理解，建议进一步学习抽象类和接口的区别。",
    "weakPoints": ["抽象类", "接口"],
    "recommendedNext": [{ "skillId": 13, "name": "Java 集合框架" }]
  }
}
```

---

### 2.6 获取技能推荐

```
GET /api/skill/recommend
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "skillId": 13,
      "skillName": "Java 集合框架",
      "categoryName": "编程语言",
      "reason": "你已掌握 Java 面向对象（80%），建议下一步学习集合框架",
      "priority": "HIGH"
    },
    {
      "skillId": 21,
      "skillName": "HTML/CSS",
      "categoryName": "前端开发",
      "reason": "作为全栈能力的基础，建议尽早开始",
      "priority": "MEDIUM"
    }
  ]
}
```

---

## 3. 简历诊断与优化 `/api/resume`

### 3.1 上传简历

```
POST /api/resume/upload
```

请求体：`multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | pdf/doc/docx，最大 10MB |
| title | String | 否 | 简历标题，默认使用文件名 |

响应：

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "resumeId": 201,
    "title": "张三的简历",
    "fileUrl": "https://your-oss.com/resumes/abc.pdf",
    "fileSize": 204800,
    "status": "UPLOADED",
    "createTime": "2026-06-22T15:00:00"
  }
}
```

---

### 3.2 获取我的简历列表

```
GET /api/resume/list?page=1&size=10
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "resumeId": 201,
        "title": "张三的简历",
        "fileUrl": "https://your-oss.com/resumes/abc.pdf",
        "status": "ANALYZED",
        "overallScore": 78,
        "createTime": "2026-06-22T15:00:00",
        "analyzeTime": "2026-06-22T15:01:30"
      }
    ],
    "total": 2,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

| status 值 | 说明 |
|-----------|------|
| UPLOADED | 已上传，待分析 |
| ANALYZING | AI 分析中 |
| ANALYZED | 分析完成 |
| ANALYZE_FAILED | 分析失败 |

---

### 3.3 获取简历详情

```
GET /api/resume/{resumeId}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "resumeId": 201,
    "title": "张三的简历",
    "fileUrl": "https://your-oss.com/resumes/abc.pdf",
    "status": "ANALYZED",
    "overallScore": 78,
    "sections": {
      "education": { "score": 85, "comment": "学历背景良好" },
      "experience": { "score": 65, "comment": "实习经历较少，建议补充项目经验" },
      "skills": { "score": 80, "comment": "技能描述清晰" },
      "summary": { "score": 70, "comment": "自我评价过于笼统" }
    },
    "createTime": "2026-06-22T15:00:00",
    "analyzeTime": "2026-06-22T15:01:30"
  }
}
```

---

### 3.4 触发 AI 分析

```
POST /api/resume/{resumeId}/analyze
```

> 调用 Dify Workflow 进行简历分析，异步执行。

响应：

```json
{
  "code": 200,
  "message": "分析已提交，请稍后查看结果",
  "data": { "resumeId": 201, "status": "ANALYZING" }
}
```

> 客户端轮询 `/api/resume/{resumeId}` 检查 status 是否变为 ANALYZED。

---

### 3.5 获取 AI 优化建议

```
POST /api/resume/{resumeId}/optimize
```

请求体：

```json
{
  "targetPosition": "Java 后端开发",
  "focusAreas": ["experience", "skills"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetPosition | String | 是 | 目标岗位 |
| focusAreas | Array | 否 | 重点关注模块：education / experience / skills / summary |

响应：

```json
{
  "code": 200,
  "message": "优化建议生成完成",
  "data": {
    "resumeId": 201,
    "targetPosition": "Java 后端开发",
    "overallSuggestion": "你的简历整体框架不错，但缺少量化成果和项目经验...",
    "sectionSuggestions": [
      {
        "section": "experience",
        "original": "在某公司实习，负责后端开发",
        "suggested": "在 XX 公司实习期间，独立完成了用户管理模块的开发，使用 Spring Boot + MyBatis-Plus 实现了 5 个 RESTful API，日均处理 1000+ 请求",
        "reason": "加入量化数据和技术栈，让招聘方更容易评估你的能力"
      }
    ]
  }
}
```

---

### 3.6 删除简历

```
DELETE /api/resume/{resumeId}
```

---

## 4. 学习数据看板 `/api/learning`

### 4.1 获取学习仪表盘数据

```
GET /api/learning/dashboard
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalStudyHours": 128.5,
      "todayStudyMinutes": 45,
      "currentStreak": 7,
      "longestStreak": 15,
      "completedSkills": 5,
      "totalSkills": 30,
      "skillProgress": 16.7,
      "resumeScore": 78
    },
    "weeklyStudyHours": [
      { "date": "2026-06-16", "hours": 2.5 },
      { "date": "2026-06-17", "hours": 3.0 },
      { "date": "2026-06-18", "hours": 1.5 },
      { "date": "2026-06-19", "hours": 4.0 },
      { "date": "2026-06-20", "hours": 2.0 },
      { "date": "2026-06-21", "hours": 3.5 },
      { "date": "2026-06-22", "hours": 0.75 }
    ],
    "categoryProgress": [
      { "categoryName": "编程语言", "progress": 62.5 },
      { "categoryName": "前端开发", "progress": 25.0 },
      { "categoryName": "后端开发", "progress": 14.3 },
      { "categoryName": "数据库", "progress": 40.0 },
      { "categoryName": "AI/机器学习", "progress": 0 }
    ]
  }
}
```

---

### 4.2 获取学习时间线

```
GET /api/learning/timeline?page=1&size=10
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "type": "SKILL_STUDY",
        "title": "学习了 Java 集合框架",
        "duration": 30,
        "detail": "完成 ArrayList 和 HashMap 的学习",
        "createTime": "2026-06-22T14:00:00"
      },
      {
        "id": 2,
        "type": "AI_CHAT",
        "title": "AI 辅导：二叉树遍历",
        "duration": 15,
        "detail": "进行了 3 轮对话",
        "createTime": "2026-06-22T13:30:00"
      }
    ],
    "total": 25,
    "current": 1,
    "size": 10,
    "pages": 3
  }
}
```

| type 值 | 说明 |
|---------|------|
| SKILL_STUDY | 技能学习 |
| AI_CHAT | AI 辅导对话 |
| RESUME_ANALYZE | 简历分析 |
| CHECKIN | 学习打卡 |

---

### 4.3 获取周报

```
GET /api/learning/report/weekly?startDate=2026-06-16&endDate=2026-06-22
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": "2026-06-16 ~ 2026-06-22",
    "totalStudyHours": 17.25,
    "studyDays": 7,
    "avgDailyHours": 2.46,
    "skillsCompleted": 2,
    "skillsInProgress": 3,
    "topSkill": "Java 集合框架",
    "aiChats": 5,
    "resumeScore": 78,
    "summary": "本周保持了连续 7 天的学习记录，完成了 2 项技能学习，建议在数据库方面加强练习。",
    "recommendations": [
      "建议增加数据库相关学习时间",
      "可以尝试做一个小项目来巩固已学技能"
    ]
  }
}
```

---

### 4.4 获取月报

```
GET /api/learning/report/monthly?year=2026&month=6
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": "2026年6月",
    "totalStudyHours": 128.5,
    "studyDays": 22,
    "avgDailyHours": 5.84,
    "skillsCompleted": 5,
    "skillsInProgress": 8,
    "categoryBreakdown": [
      { "categoryName": "编程语言", "hours": 52.0 },
      { "categoryName": "前端开发", "hours": 28.5 },
      { "categoryName": "数据库", "hours": 35.0 },
      { "categoryName": "AI/机器学习", "hours": 13.0 }
    ],
    "growthTrend": "IMPROVING",
    "summary": "本月学习状态良好，编程语言进步明显，建议下月重点攻克后端开发技能。"
  }
}
```

---

## 5. 职业规划 `/api/career`

### 5.1 获取职业规划

```
GET /api/career/plan
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "planId": 1,
    "targetCareer": "Java 后端开发工程师",
    "currentStage": "在校学习",
    "timeline": [
      {
        "phase": "大三下学期",
        "goals": ["掌握 Spring Boot 全栈开发", "完成 2 个实战项目"],
        "status": "IN_PROGRESS"
      },
      {
        "phase": "大四上学期",
        "goals": ["参加秋招", "获得实习 Offer"],
        "status": "NOT_STARTED"
      }
    ],
    "skillGap": [
      { "skillName": "Redis", "importance": "HIGH", "currentLevel": 0 },
      { "skillName": "Docker", "importance": "MEDIUM", "currentLevel": 0 }
    ],
    "createTime": "2026-06-20T10:00:00",
    "updateTime": "2026-06-22T14:00:00"
  }
}
```

---

### 5.2 更新职业规划

```
PUT /api/career/plan
```

请求体：

```json
{
  "targetCareer": "Java 后端开发工程师",
  "currentStage": "在校学习",
  "timeline": [
    {
      "phase": "大三下学期",
      "goals": ["掌握 Spring Boot", "完成实战项目"],
      "status": "IN_PROGRESS"
    }
  ]
}
```

---

### 5.3 获取 AI 职业推荐

```
POST /api/career/recommend
```

请求体：

```json
{
  "interests": ["后端开发", "人工智能"],
  "currentSkills": ["Java", "MySQL", "Spring Boot"]
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "recommendations": [
      {
        "career": "Java 后端开发工程师",
        "matchRate": 85,
        "requiredSkills": ["Java", "Spring Boot", "MySQL", "Redis", "Docker"],
        "missingSkills": ["Redis", "Docker"],
        "salaryRange": "8K-15K",
        "description": "负责服务端业务逻辑开发，需要扎实的 Java 基础和数据库能力"
      }
    ]
  }
}
```

---

## 6. 学习打卡 `/api/checkin`

### 6.1 今日打卡

```
POST /api/checkin
```

请求体：

```json
{
  "duration": 45,
  "note": "今天学习了二叉树遍历"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| duration | Integer | 是 | 学习时长（分钟） |
| note | String | 否 | 学习笔记，不超过 200 字 |

响应：

```json
{
  "code": 200,
  "message": "打卡成功",
  "data": {
    "checkinId": 1,
    "date": "2026-06-22",
    "duration": 45,
    "streak": 8,
    "totalCheckins": 22
  }
}
```

---

### 6.2 获取打卡记录

```
GET /api/checkin/records?year=2026&month=6
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      { "date": "2026-06-01", "duration": 60, "streak": 1 },
      { "date": "2026-06-02", "duration": 45, "streak": 2 }
    ],
    "totalCheckins": 22,
    "currentStreak": 8,
    "longestStreak": 15,
    "monthTotalMinutes": 1200
  }
}
```

---

## 接口总览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| AI 辅导 | POST | /api/chat/session | 创建会话 |
| AI 辅导 | GET | /api/chat/sessions | 会话列表 |
| AI 辅导 | GET | /api/chat/session/{id}/messages | 消息列表 |
| AI 辅导 | POST | /api/chat/message | 发送消息（SSE） |
| AI 辅导 | DELETE | /api/chat/session/{id} | 删除会话 |
| 技能树 | GET | /api/skill/categories | 技能分类 |
| 技能树 | GET | /api/skill/tree | 技能树 |
| 技能树 | GET | /api/skill/user/progress | 我的进度 |
| 技能树 | PUT | /api/skill/user/progress | 更新进度 |
| 技能树 | POST | /api/skill/evaluate | AI 评估 |
| 技能树 | GET | /api/skill/recommend | 技能推荐 |
| 简历 | POST | /api/resume/upload | 上传简历 |
| 简历 | GET | /api/resume/list | 简历列表 |
| 简历 | GET | /api/resume/{id} | 简历详情 |
| 简历 | POST | /api/resume/{id}/analyze | 触发 AI 分析 |
| 简历 | POST | /api/resume/{id}/optimize | AI 优化建议 |
| 简历 | DELETE | /api/resume/{id} | 删除简历 |
| 数据看板 | GET | /api/learning/dashboard | 仪表盘 |
| 数据看板 | GET | /api/learning/timeline | 学习时间线 |
| 数据看板 | GET | /api/learning/report/weekly | 周报 |
| 数据看板 | GET | /api/learning/report/monthly | 月报 |
| 职业规划 | GET | /api/career/plan | 获取规划 |
| 职业规划 | PUT | /api/career/plan | 更新规划 |
| 职业规划 | POST | /api/career/recommend | AI 推荐 |
| 打卡 | POST | /api/checkin | 今日打卡 |
| 打卡 | GET | /api/checkin/records | 打卡记录 |