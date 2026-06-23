# AI伴学与职业成长平台 — 全局开发规范

## 项目定位

高校学生 AI 伴学与职业成长平台（课程实训项目）。提供 AI 学业辅导、技能树管理、简历诊断优化、学习数据可视化、职业成长规划。

**优先保证**：功能完整 → 架构清晰 → 三端联通 → AI 能力可用。避免过度设计。

**评分权重**：功能实现 50% | 创新性 20% | 界面设计 20% | 文档展示 10%

## 技术栈

| 层 | 技术 |
|---|---|
| 移动端 | HarmonyOS NEXT + ArkTS + ArkUI |
| 管理后台 | Vue 3 + Vite + Element Plus + ECharts (JS) |
| 后端 | Java 17 · Spring Boot 3 · Spring AI · MyBatis-Plus · MySQL · JWT |
| AI 能力 | Dify (Workflow + Chatflow) + Xiaomi MiMo API |

## 架构原则

**分层调用**：Controller → Service → Mapper → Database，禁止 Controller 直接操作数据库，业务逻辑必须在 Service 层。

**统一机制**：统一返回 `Result<T>` + `GlobalExceptionHandler` + JWT 鉴权。

## 代码规范

**命名**：类名 `UserService` / `ResumeController`；方法 `getUserInfo()` / `createResume()`；变量 `userId` / `skillName`。

**DTO**：请求 `xxxRequest`，返回 `xxxResponse`，禁止直接暴露 Entity。

**数据库**：必须含 `id`, `create_time`, `update_time`, `is_deleted`（逻辑删除）。

**SQL**：禁止 `SELECT *`、N+1 查询、无分页列表查询。分页使用 MyBatis-Plus `Page`。

## 开发流程（按任务类型裁剪）

```
任务判断
├─ QUERY（纯咨询）─────────────→ 直接回答
├─ TRIVIAL（小 bug / 改文案）──→ 分析 → 编码 → 自测
├─ FEATURE（新功能）───────────→ requirements → design → 测试先行 → 编码 → 门禁 → 验收
└─ ARCHITECTURE（架构改动）────→ requirements → design → 风险分析 → TDD → 全链路回归验证
```

### FEATURE / ARCHITECTURE 标准流程

1. **需求确认**（`requirements.md`）：功能目标 → 数据模型 → API 设计 → 页面交互 → 验收标准（正例 + 反例）
2. **技术方案**（`design.md`）：现状 → 目标 → 改动点 → 风险 → 验收方式。禁止跳过设计阶段
3. **测试先行**（TDD）：先写测试定义"什么是对的"，测试需经 review，再写实现
4. **编码实现**：按方案实现，禁止编造不存在的表/接口/字段
5. **门禁验证**：G1 通过 → G2 通过 → G3 通过 → 提交
6. **验收输出**：改动文件 + 实现说明 + 测试方式 + 风险点

**禁止**：跳过设计直接编码；猜测实现；大规模重构已有代码；修改未授权模块。

## 质量门禁

| 门禁 | 检查项 | 失败退回 |
|------|--------|---------|
| G1 | 编译 / lint 通过 | 退回修改 |
| G2 | 测试通过 + 无明显异常 | 退回开发 |
| G3 | 接口可调用 + 前后端联调通过 | 退回联调 |

任何门禁失败必须修复后重跑，不可跳过。

## 硬规则

- **TDD 顺序不可跳过**：FEATURE / ARCHITECTURE 任务必须测试先行
- **改完必验证**：检测到业务代码改动必须跑完门禁才能提交
- **反馈留痕**：每次纠正 AI 的错误，记录到 `feedback.md`（场景 → 纠正 → 沉淀）
- **上下文不足时**：优先分析现有代码，禁止猜测，禁止编造

## 实训优先级

| 优先级 | 模块 |
|--------|------|
| **P0**（先做） | 用户体系 · AI 辅导 · 简历优化 · 技能树 · 数据看板 |
| **P1** | 学习路径推荐 · 成长报告 · 职业规划 |
| **P2** | 社区功能 · 打卡功能 · 其他创新功能 |

先完成 P0 再扩展 P1/P2。

## 核心原则

> 上下文先行 · 设计先行 · 测试先行 · 小步迭代 · 先做正确再做复杂 · 以验收通过为第一目标
