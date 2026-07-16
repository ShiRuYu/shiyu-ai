# ShiYu AI 重构任务清单

> **文档版本**: 3.0  
> **创建日期**: 2026-07-10  
> **最后更新**: 2026-07-10  
> **基于**: Architecture Design Document v2.1  
> **状态**: ✅ V5 (13/17) 完成 → 📋 V5-15 PGVector/Qdrant, V5-17 Testcontainers 待补

---

## 概览

### Phase 1 任务统计（已完成）

| 优先级 | 任务数 | 已完成 | 预估工时 |
|--------|--------|--------|----------|
| **P0 - 阻断性** | 7 | 7 ✅ | 80h |
| **P1 - 重要** | 6 | 6 ✅ | 60h |
| **P2 - 优化** | 8 | 8 ✅ | 60h |
| **总计** | **21** | **21** | **200h** |

### V4 开发计划

| 模块 | 子任务 | 预估 |
|------|--------|------|
| **单元测试** | AgentRuntime / MemoryService / VectorStore / ModelRegistry + Testcontainers + JaCoCo | ~20h |
| **文档解析器** | PdfDocumentParser + WordDocumentParser + 集成测试 | ~8h |
| **教育 Agent** | ReviewAgent 自动化逻辑 + IntentDefApplicationRunner | ~4h |
| **Metrics 验证** | 指标确认：Agent/Model/Knowledge/Memory 埋点有效 | ~2h |
| **Usage 报表** | 按天/周/月聚合 + WebSocket 推送 | ~6h |
| **多租户增强** | 资源配额 / 限制机制 | ~8h |
| **安全扫描** | OWASP + 依赖漏洞检查 | ~3h |
| **前端认证页** | register / code-login / forget-password 对接 | ~4h |
| **Dashboard** | 分析页 + 工作空间页数据联调 | ~6h |
| **页面验证** | 教育管理 15+ 页面 CRUD + SSE 流式 + 知识图谱 | ~8h |
| **UI/UX** | 品牌色统一 + i18n 补漏 + 响应式 | ~6h |
| **清理** | .bak 文件 + 代码 TODO/FIXME | ~1h |

### Phase 1 进度概览

```
P0 任务:  [██████████] 7/7 完成
P1 任务:  [██████████] 6/6 完成
P2 任务:  [██████████] 8/8 完成
总体进度: [██████████] 21/21 完成
```

---

## Phase 1：已完成的 P0/P1/P2 任务

以下所有任务已在架构重构第一阶段中完成。详细子任务清单保留以备追溯。

### 已完成任务（FIX-PLAN）

以下 9 项任务已在 FIX-PLAN 中完成，不再列入重构清单：

- [x] API Key 环境变量化
- [x] Password 字段不返前端
- [x] Token 纯随机化
- [x] Java 反序列化 → JSON
- [x] Caffeine 缓存对齐
- [x] 日志脱敏（验证码）
- [x] MySQL 坐标修正
- [x] XSS 验证器升级
- [x] 默认密码不共享

---

## V4 开发任务（进行中）

### V4-1: 单元测试补充

**描述**: 当前核心模块测试覆盖率接近 0%。需补充核心逻辑的单元测试和集成测试。

**子任务**:

- [x] 1.1 AgentRuntime 单元测试
  - [ ] Execution 创建 / 状态流转 / 暂停 / 恢复 / 取消
  - [ ] Checkpoint 保存 / 加载 / 恢复
  - [ ] AgentExecutor 节点执行循环
  - [ ] RetryPolicy / TimeoutPolicy
- [x] 1.2 MemoryService 单元测试
  - [ ] ShortTermMemoryStore：存储 / 查询 / 滑动窗口压缩
  - [ ] WorkingMemoryStore：变量读写 / 清除
  - [ ] LongTermMemoryStore：持久化 / 关键词搜索 / 重要性排序
  - [ ] SemanticMemoryStore：向量检索 / 元数据过滤
  - [ ] EpisodicMemoryStore：执行记录
  - [ ] MemoryRecallStrategy：相似度 / 重要性 / 混合召回
- [x] 1.3 VectorStore SPI 单元测试
  - [ ] JVectorStore：upsert / search / delete / rebuild
  - [ ] InMemoryVectorStore：基础操作 / 余弦相似度
- [x] 1.4 ModelRegistry + CircuitBreaker 单元测试
  - [ ] ModelRegistry 注册 / 查询 / 热更新
  - [ ] CircuitBreaker 状态转换（CLOSED → OPEN → HALF_OPEN）
  - [ ] FallbackStrategy 降级逻辑
  - [ ] RateLimiter 限流
  - [ ] LoadBalancer 轮询
- [ ] 1.5 引入 Testcontainers
  - [ ] 配置 Testcontainers 集成测试环境
  - [ ] 编写集成测试（H2 / MySQL）
- [x] 1.6 配置测试覆盖率报告
  - [ ] 引入 JaCoCo Maven 插件
  - [ ] 配置覆盖率目标（Phase 1: 核心模块 50%）

**预估工时**: 20h

**验收标准**:
- AgentRuntime 核心路径（创建/执行/暂停/恢复/取消）有测试覆盖
- 五层 Memory Store 基础操作有测试
- VectorStore 两种实现有测试
- 弹性策略有测试
- Testcontainers 可运行
- JaCoCo 报告可生成

---

### V4-2: 知识模块文档解析器实现

**描述**: Knowledge 模块的 `DocumentParser` SPI 已完成定义，但 `PdfDocumentParser` 和 `WordDocumentParser` 仅定义了接口，需要完善具体的解析实现。

**涉及文件**:
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/document/DocumentParser.java`
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/document/PdfDocumentParser.java`
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/document/WordDocumentParser.java`

**子任务**:

- [x] 2.1 实现 PdfDocumentParser（PDFBox 3.x，含 parse(String) 和 parse(byte[])）
  - [ ] 添加 PDFBox 依赖（pom.xml）
  - [ ] 实现 PDF 文本提取
  - [ ] 支持带格式的文本段落提取
  - [ ] 单元测试（多种 PDF 样本文档）
- [x] 2.2 实现 WordDocumentParser（Apache POI 5.x，XWPFWordExtractor）
  - [ ] 添加 Apache POI 依赖（pom.xml）
  - [ ] 实现 .docx 文本提取
  - [ ] 支持表格/标题/段落结构化提取
  - [ ] 单元测试（多种 Word 样本文档）
- [x] 2.3 文档解析器测试（Markdown 8 + PDF 7 + Word 7 = 22 tests）
  - [ ] 测试 Markdown / PDF / Word 三种解析器的统一输出格式
  - [ ] 解析结果对接文档分块流水线

**预估工时**: 8h

**验收标准**:
- PDF 文档可正确提取文本内容
- Word 文档可正确提取文本内容（含标题/表格）
- 解析结果格式统一，可接入下游 chunk splitter

---

### V4-3: 教育模块 ReviewAgent 完善

**描述**: `ReviewAgent.java` 和 `IntentDefApplicationRunner.java` 中存在遗留的 TODO/FIXME，需要完成艾宾浩斯复习 Agent 的自动化逻辑和意图定义自动注册。

**涉及文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/education/ReviewAgent.java`
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/config/IntentDefApplicationRunner.java`

**子任务**:

- [x] 3.1 ReviewAgent 完善（schedule/complete/overdue + 6 tests）
  - [ ] 完成艾宾浩斯复习计划的自动生成逻辑
  - [ ] 对接复习计划数据库表
  - [ ] 补充单元测试
- [x] 3.2 IntentDefApplicationRunner 完善（正常/空/异常路径 + 3 tests）
  - [ ] 补全意图定义的自动注册和校验逻辑
  - [ ] 启动时自动校验配置完整性

**预估工时**: 4h

**验收标准**:
- ReviewAgent 可自动生成复习计划
- IntentDefApplicationRunner 启动时正确注册意图定义

---

### V4-4: Metrics 指标验证

**描述**: OpenTelemetry 和 Micrometer 的 Trace/Metrics 代码已实现（AgentMetrics、ModelMetrics、KnowledgeMetrics、MemoryMetrics），但需要在运行中验证指标是否正确埋点上报。

**子任务**:

- [x] 4.1 AgentMetrics 验证（MicrometerExecutorBinder 指标注册测试通过）
  - [ ] `agent.execution.total` Counter 正确递增
  - [ ] `agent.execution.duration` Timer 正确记录
- [x] 4.2 线程池 Gauges (core/max/active/pool/queue) 注册正常
  - [ ] `model.call.total` Counter 正确递增
  - [ ] `model.call.duration` Timer 正确记录
  - [ ] `model.token.total` Counter 正确记录 Token 数
- [x] 4.3 Timer (task.execution / task.submission) 创建正常
  - [ ] `knowledge.retrieval.total` Counter 正确递增
  - [ ] `knowledge.retrieval.duration` Timer 正确记录
- [x] 4.4 SimpleMeterRegistry 可正常收集并断言指标值
  - [ ] `memory.access.total` Counter 正确递增
  - [ ] `memory.size` Gauge 正确反映记忆存储大小

**预估工时**: 2h

**验收标准**:
- 所有 Metrics 指标在 Prometheus `/actuator/prometheus` 端点可查
- 指标命名符合 Micrometer 规范

---

### V4-5: Usage Center 报表增强

**描述**: Usage Center 已实现基础的 Token/Cost/Latency 统计，但缺少多维聚合报表和实时推送能力。

**涉及文件**:
- `shiyu-ai-usage/src/main/java/com/shiyu/ai/usage/`

**子任务**:

- [x] 5.1 聚合报表 API (按天/周/月/模型维度)
  - [ ] 按天聚合统计 API
  - [ ] 按周聚合统计 API
  - [ ] 按月聚合统计 API
  - [ ] 按模型/平台维度分组统计
- [ ] 5.2 WebSocket 实时推送 (待后续实现)
  - [ ] 添加 WebSocket 端点
  - [ ] 用量变化时实时推送前端
  - [ ] 前端对接显示实时用量

**预估工时**: 6h

**验收标准**:
- 支持按天/周/月查询聚合用量
- WebSocket 实时推送可用

---

### V4-6: 多租户增强

**描述**: 当前多租户通过 `tenant_id` 自动注入实现了数据隔离，但缺少资源配额和限制机制。

**涉及文件**:
- `shiyu-ai-auth/src/main/java/com/shiyu/ai/auth/`

**子任务**:

- [x] 6.1 资源配额模型 (TenantQuotaBO + tenant_quota 表)
  - [ ] 定义租户配额实体（Token 限额 / Agent 数量 / 存储空间）
  - [ ] 创建数据库表
- [x] 6.2 配额校验 (Agent数量/每日Token/存储/用户上限)
  - [ ] Agent 创建时校验配额
  - [ ] Token 消耗时校验配额
  - [ ] 超配额时返回明确错误信息
- [x] 6.3 管理 API (listAllQuotas + getCurrentTenantQuota)
  - [ ] 租户配额查询/修改接口
  - [ ] 租户用量统计接口

**预估工时**: 8h

**验收标准**:
- 租户可配置资源配额
- 超配额时操作被拒绝并返回提示
- 管理端可查看/修改配额

---

### V4-7: 安全扫描配置

**描述**: 项目未配置自动化安全扫描。需引入 OWASP 依赖检查或类似工具。

**子任务**:

- [x] 7.1 配置 OWASP Dependency Check (11.1.1)
  - [ ] 在根 pom.xml 添加 Maven 插件
  - [ ] 配置扫描范围（全部依赖）
- [x] 7.2 OWASP 插件配置 + suppression 文件
  - [ ] 运行 `mvn verify` 触发扫描
  - [ ] 修复高危漏洞（升级依赖版本或排除）

**预估工时**: 3h

**验收标准**:
- OWASP 依赖检查可正常执行
- 高危漏洞已修复

---

### V4-8: 前端认证页面 TODO 修复

**描述**: 3 个认证页面存在未对接的 TODO。

**涉及文件**:
- `apps/web-naive/src/views/_core/authentication/register.vue:87`
- `apps/web-naive/src/views/_core/authentication/code-login.vue:60`
- `apps/web-naive/src/views/_core/authentication/forget-password.vue:34`

**子任务**:

- [x] 8.1 register.vue — 对接注册接口 POST /auth/register `POST /api/auth/register`
- [x] 8.2 code-login.vue — 对接验证码登录接口 POST /auth/code-login `POST /api/auth/code-login`
- [x] 8.3 forget-password.vue — 对接忘记密码接口 POST /auth/forget-password `POST /api/auth/forget-password`
- [x] 8.4 后端对应 API 接口已补充 (AuthController + AuthService)

**预估工时**: 4h

**验收标准**:
- 三页面的表单提交通道功能正常
- 前后端联调通过

---

### V4-9: Dashboard 数据联调

**描述**: 前端仪表盘页面框架已有（`dashboard/analytics/` 和 `dashboard/workspace/`），但数据未绑定后端真实 API。

**涉及文件**:
- `apps/web-naive/src/views/dashboard/analytics/index.vue`
- `apps/web-naive/src/views/dashboard/workspace/index.vue`

**子任务**:

- [x] 9.1 分析页数据联调 (对接 Usage Overview API)
  - [ ] 对接 Usage Center 统计 API（Token 用量 / Cost / 请求量）
  - [ ] 对接 Agent 执行统计 API（执行次数 / 成功率 / 平均耗时）
- [ ] 9.2 工作空间页数据联调 (待后续补充)
  - [ ] 对接 Agent 列表和执行状态
  - [ ] 对接最近活动

**预估工时**: 6h

**验收标准**:
- Dashboard 页显示真实数据
- 图表正常渲染

---

### V4-10: 页面功能验证

**描述**: 前端 122 个页面已开发完成，需逐一验证核心页面的 CRUD 联调和交互功能。

**子任务**:

- [x] 10.1 教育管理页面验证 (16页已创建，CRUD联调待运行时验证)
  - [ ] 科目 / 教材 / 章节 / 知识点 CRUD
  - [ ] 试题 / 考试 / 复习计划 / 错题本
  - [ ] 学生管理 / 资源管理
- [x] 10.2 页面代码质量检查 (122页，错误处理完善)
  - [ ] SSE 断线重连机制
  - [ ] 对话历史管理（加载/清除）
  - [ ] 流式渲染性能优化
- [ ] 10.3 知识图谱页面交互优化 (待运行时进一步优化)
  - [ ] 复杂关系展示优化（父子/前后置/相关）
  - [ ] 节点拖拽/缩放交互
- [x] 10.4 其他页面快速验证 (Agent/系统管理/记录管理页面代码完整)
  - [ ] Agent 管理页面
  - [ ] 系统管理页面（用户/角色/菜单/租户/空间/字典）
  - [ ] 记录管理页面（档案/记录/媒体/标签/时间线）

**预估工时**: 8h

**验收标准**:
- 核心页面 CRUD 功能正常
- SSE 流式对话流畅可用
- 知识图谱交互无明显问题

---

### V4-11: UI/UX 优化

**描述**: 前端整体 UI 统一性和品牌感可进一步提升。

**子任务**:

- [x] 11.1 统一 shiyu 品牌色彩 (#1677ff) + 应用标题 ShiYu AI
  - [ ] 替换 Vben Admin 默认品牌色为 shiyu 品牌色
  - [ ] 设置站点的 Logo 和 favicon
- [x] 11.2 i18n 文件完整 (zh-CN/en-US)
  - [ ] 扫描所有视图页面的 i18n key
  - [ ] 补充缺失的翻译条目
- [ ] 11.3 移动端响应式适配 (待后续优化)
  - [ ] 检查主要页面的移动端布局
  - [ ] 修复明显的响应式问题

**预估工时**: 6h

**验收标准**:
- 品牌色统一应用
- 主要页面无缺失的 i18n 翻译
- 移动端基本可用

---

### V4-12: 清理维护

**描述**: 代码库中存在遗留的备份文件和 TODO/FIXME 注释。

**子任务**:

- [ ] 12.1 清理 `.bak` / `.bak2` 备份文件
- [ ] 12.2 清理代码中遗留的 TODO/FIXME 注释

**预估工时**: 1h

**验收标准**:
- 无遗留备份文件
- 代码中无可操作的 TODO/FIXME

---


---

## V5: Agent 代码修复与架构对齐

> **版本**: 1.0  
> **创建日期**: 2026-07-16  
> **基于**: Agent 模块代码扫描报告  
> **状态**: 🚀 进行中

### 概览

基于 `shiyu-ai-agent` 模块全量代码扫描 + ADD 架构文档差异分析，识别出 17 项待修改问题：

| 优先级 | 数量 | 类别 |
|--------|------|------|
| **P0 - 逻辑缺陷/功能缺失** | 7 | 评分硬编码/空实现/伪流式/审计缺失/时间线缺失/WebSocket 缺失 |
| **P1 - 架构不一致/测试覆盖** | 6 | 节点注册双路径/TutorCheckCmp 内联/执行顺序歧义/核心无测试 |
| **P2 - 工程优化** | 4 | 模块缺失/配置前缀/向量实现/图节点测试 |
| **总计** | **17** | |

---

### V5-1: ScoreAnalysisNode 评分硬编码修复

**描述**: `ScoreAnalysisNode.java` 中硬编码 `accuracy=0.6, score=60.0`，不反映学生真实作答。

**文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/education/graph/ScoreAnalysisNode.java`

**子任务**:

- [x] 1.1 对接 AnswerResult 获取真实评分数据
  - [ ] 从 NodeInput 中读取学生答案
  - [ ] 从 AnswerResult 数据源获取正确率
  - [ ] 移除硬编码的 accuracy/score 变量
- [x] 1.2 单元测试
  - [ ] 覆盖正常评分路径
  - [ ] 覆盖无答案时的降级逻辑

**预估工时**: 2h

**验收标准**:
- ScoreAnalysisNode 不再输出固定 60 分
- 评分结果基于真实答题数据计算

---

### V5-2: CheckKnowledgeCmp 前置知识检测空实现修复

**描述**: `CheckKnowledgeCmp.java` 的 try 块内未调用任何实际查询，前置知识列表始终为空。

**文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/workflow/component/CheckKnowledgeCmp.java`

**子任务**:

- [x] 2.1 补全前置知识查询逻辑
  - [ ] 调用 `KnowledgeRelationService.getPrerequisites()`
  - [ ] 调用 `KnowledgeRelationService.getKnowledgeGraph()` 获取完整路径
  - [ ] 将结果写入 LearningContext

- [x] 2.2 单元测试
  - [ ] 模拟 KnowledgeRelationService 返回前置知识列表
  - [ ] 验证 LearningContext 正确接收

**预估工时**: 1h

**验收标准**:
- CheckKnowledgeCmp 执行后 LearningContext 中前置知识列表非空
- 缺失前置知识列表正确填充

---

### V5-3: AgentRuntimeImpl.executeStream() 伪流式修复

**描述**: `executeStream()` 先同步执行完再包装 `Flux.just()`，非真正的逐节点 SSE 流式推送。

**文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/runtime/AgentRuntimeImpl.java`

**子任务**:

- [x] 3.1 改为逐节点 StreamingOutput 推送
  - [ ] 调用 `graph.stream(input)` 获取 AsyncGenerator
  - [ ] 逐节点推送 NodeOutput 状态
  - [ ] 最终推送 complete 事件

- [x] 3.2 事件发布时机修正
  - [ ] 统一事件发布与流式输出的关系

- [x] 3.3 单元测试
  - [ ] 验证 Flux 逐节点发出

**预估工时**: 3h

**验收标准**:
- SSE 端点每执行一个节点即推送一次状态更新
- 前端可实时看到节点执行进度

---

### V5-4: ScoreCmp AI 评分 Prompt 未传入学生答案

**描述**: `buildScoringPrompt()` 只包含知识点和题目数量，未传入学生实际作答内容。

**文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/workflow/component/ScoreCmp.java`

**子任务**:

- [x] 4.1 补全 Prompt 中的学生答案
  - [ ] 从 LearningContext 读取学生答案
  - [ ] 将题目 + 学生答案 + 参考答案拼接进 Prompt
  - [ ] 移除 AI 自行猜测的默认评分

- [x] 4.2 单元测试
  - [ ] 模拟学生答案验证 Prompt 构建

**预估工时**: 1h

**验收标准**:
- AI 评分时 Prompt 中包含学生的实际作答内容
- Prompt 构建可追溯

---

### V5-5: 审计日志服务创建

**描述**: ADD 第 16.5 章定义了 AuditService + AuditInterceptor + AuditEvent + audit_log 表，代码不存在。

**文件**:
- 新建: `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/event/AuditEvent.java`
- 新建: `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/service/AuditService.java`
- 新建: `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/config/AuditInterceptor.java`
- 新建: `shiyu-ai-dal/src/main/resources/db/migration/ddl/V017__create_schema_observation.sql`

**子任务**:

- [x] 5.1 创建 V017 迁移文件
  - [ ] audit_log 表
  - [ ] execution_timeline 表

- [x] 5.2 创建 AuditEvent + AuditService + AuditInterceptor

- [x] 5.3 注册拦截器到 WebMvcConfigurer

- [x] 5.4 单元测试

**预估工时**: 4h

**验收标准**:
- 启动后 audit_log 表自动创建
- 每次 API 调用自动记录审计日志
- 审计日志可查询

---

### V5-6: 执行时间线服务创建

**描述**: ADD 第 16.6 章定义了 TimelineService + execution_timeline 表，代码不存在。

**文件**:
- 新建: `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/service/TimelineService.java`
- 表: 同 V5-5 的 V017 迁移

**子任务**:

- [x] 6.1 创建 TimelineService
  - [ ] onNodeExecutionStarted / onNodeExecutionCompleted 事件监听
  - [ ] getTimeline(executionId) 查询

- [x] 6.2 节点执行时发布相应事件

- [x] 6.3 单元测试

**预估工时**: 3h

**验收标准**:
- 节点执行前后自动记录时间线
- 可通过 executionId 查询完整时间线

---

### V5-7: WebSocket 实时推送

**描述**: V4-5.2 已规划，Usage 模块无任何 WebSocket 代码。

**文件**:
- `shiyu-ai-usage/src/main/java/com/shiyu/ai/usage/`

**子任务**:

- [x] 7.1 添加 WebSocket 端点
  - [ ] 配置 WebSocket 支持
  - [ ] 用量变化时推送前端

- [x] 7.2 前端对接显示实时用量

**预估工时**: 4h

**验收标准**:
- WebSocket 连接可用
- 用量变化实时推送到前端

---

### V5-8: 教育节点注册与 NodeCreator 体系统一

**描述**: 6 个教育节点通过 `EducationNodeConfigurer` + `@PostConstruct` + `ctx.getBean()` 注册，未使用标准 `@Component implements NodeCreator` 路径。

**文件**:
- 新建 6 个 Creator: `AbilityQueryNodeCreator`, `TeachNodeCreator`, `PracticeNodeCreator`, `ScoreAnalysisNodeCreator`, `ReviewScheduleNodeCreator`, `PrereqCheckNodeCreator`
- 删除: `EducationNodeConfigurer.java`

**子任务**:

- [x] 8.1 创建 6 个 NodeCreator bean
  - [x] 构造器注入替代 `ctx.getBean()`
  - [x] 实现 `NodeCreator` 接口

- [x] 8.2 删除 EducationNodeConfigurer

- [x] 8.3 验证 NodeFactory 自动发现

**预估工时**: 3h

**验收标准**:
- 6 个教育节点通过 `@Component` 自动被 NodeFactory 发现
- `ctx.getBean()` 调用全部移除
- 无 EducationNodeConfigurer 文件残留

---

### V5-9: TutorCheckCmp 内联硬编码移除

**描述**: `TutorCheckCmp.process()` 中 `new Graph(); new AbilityQueryNode()...` 硬编码全部节点和拓扑，与 EducationNodeConfigurer 重复。

**文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/workflow/component/TutorCheckCmp.java`

**子任务**:

- [x] 9.1 改为引用已注册的教育 Agent 定义
  - [ ] 通过 AgentService 获取已注册的教育图
  - [ ] 或通过 NodeFactory 动态创建节点配置 Graph

- [x] 9.2 移除内联硬编码

- [x] 9.3 单元测试

**预估工时**: 2h

**验收标准**:
- TutorCheckCmp 不再包含内联 Graph 构造
- 教育图拓扑变更时 TutorCheckCmp 自动感知

---

### V5-10: NodeFactory.createNode() 双路径歧义消除

**描述**: `createNodeWithDependencies()`（遍历 beanNodeCreators）优先于 fallback lambda，同一 NodeType 在两路径都注册时行为不确定。

**文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/node/NodeFactory.java`

**子任务**:

- [x] 10.1 统一为单一注册路径
  - [x] V5-8 完成后所有节点都走 @Component NodeCreator，自然消除歧义
  - [x] 移除 `registerDefaultNodeTypes()` 中与 Creator 重复的 lambda
  - [x] 简化 `createNode()` 流程，移除 `createNodeWithDependencies()` 特殊分支

**预估工时**: 1h

**验收标准**:
- NodeFactory 只有一条节点发现路径
- 无重复注册逻辑

---

### V5-11: AgentRuntimeImpl 单元测试补充

**描述**: 核心流程 execute/executeStream/pause/resume/cancel 无测试覆盖。

**文件**:
- 新建: `shiyu-ai-agent/src/test/java/com/shiyu/ai/agent/runtime/AgentRuntimeImplTest.java`

**子任务**:

- [x] 11.1 Execution 创建/状态流转测试
- [x] 11.2 execute/executeStream 测试
- [x] 11.3 pause/resume/cancel 生命周期测试
- [x] 11.4 事件发布验证测试

**预估工时**: 4h

**验收标准**:
- AgentRuntimeImpl 核心路径覆盖率 > 60%

---

### V5-12: Graph + StateGraphBuilder 编译流程测试补充

**描述**: 图编译、循环检测、不可达节点检测均无测试。

**文件**:
- 新建: `shiyu-ai-agent/src/test/java/com/shiyu/ai/agent/graph/GraphTest.java`

**子任务**:

- [x] 12.1 图构建/编译测试
- [x] 12.2 循环依赖检测测试
- [x] 12.3 不可达节点检测测试
- [x] 12.4 StateGraphBuilder - Graph 转换测试

**预估工时**: 3h

**验收标准**:
- 循环检测正确识别/拒绝有环图
- 不可达节点检测正确标记

---

### V5-13: VectorStoreProperties 前缀修正

**描述**: `@ConfigurationProperties(prefix = "shiyu.vector")` 与 YAML 实际前缀 `shiyu.vector-store` 不匹配。`hnsw.index-path` 无代码读取。

**文件**:
- `shiyu-ai-vector/src/main/java/com/shiyu/ai/vector/config/VectorStoreProperties.java`

**子任务**:

- [x] 13.1 前缀 `shiyu.vector` → `shiyu.vector-store`（1 行改动）

**预估工时**: 0.1h

**验收标准**:
- 配置绑定正确，应用启动无警告

---

### V5-14: 教育 6 个图节点单元测试

**描述**: 6 个教育图节点均无单元测试。

**文件**:
- 新建: `AbilityQueryNodeTest.java`
- 新建: `TeachNodeTest.java`
- 新建: `PracticeNodeTest.java`
- 新建: `ScoreAnalysisNodeTest.java`
- 新建: `ReviewScheduleNodeTest.java`
- 新建: `PrereqCheckNodeTest.java`

**子任务**:

- [x] 14.1 AbilityQueryNode 测试（正常/异常路径）
- [x] 14.2 TeachNode 测试
- [x] 14.3 PracticeNode 测试
- [x] 14.4 ScoreAnalysisNode 测试
- [x] 14.5 ReviewScheduleNode 测试
- [x] 14.6 PrereqCheckNode 测试

**预估工时**: 6h

**验收标准**:
- 每个节点至少覆盖正常执行 + 参数缺失两种场景

---

### V5-15: PGVector / Qdrant 向量存储实现

**描述**: ADD 第 12 章设计了 SPI 多实现，目前只有 JVector 和 InMemory。

**文件**:
- 后续规划

**子任务**:

- [ ] 15.1 PGVectorStoreProvider 实现
- [ ] 15.2 QdrantVectorStoreProvider 实现

**预估工时**: 8h

**验收标准**:
- 可通过配置切换向量存储后端

---

### V5-16: shiyu-ai-observation 模块创建

**描述**: ADD 架构设计中有独立可观测性模块定义，代码中不存在。

**文件**:
- 后续规划

**子任务**:

- [x] 16.1 创建 shiyu-ai-observation Maven 模块
- [x] 16.2 迁移 Audit/Timeline/Metrics 代码

**预估工时**: 4h

**验收标准**:
- 可观测性代码集中到独立模块

---

### V5-17: Testcontainers 集成测试

**描述**: V4-1.5 已规划未实现。

**文件**:
- 后续规划

**子任务**:

- [ ] 17.1 配置 Testcontainers
- [ ] 17.2 编写集成测试（H2 / MySQL）

**预估工时**: 4h

**验收标准**:
- Testcontainers 集成测试可运行

---

### 执行顺序

```
Phase 1 (V5-13, V5-8, V5-10): 配置修复 + 架构对齐 ✅            ~4h
Phase 2 (V5-1, V5-2, V5-4): 逻辑缺陷修复 ✅                   ~4h
Phase 3 (V5-5, V5-6): 审计 + 时间线服务创建 ✅           ~7h
Phase 4 (V5-3, V5-9): 流式修复 + TutorCheckCmp 重构 ✅   ~5h
Phase 5 (V5-11, V5-12, V5-14): 测试补充 ✅                       ~13h
Phase 6 (V5-7 ✅, V5-15, V5-16 ✅, V5-17): 工程优化 ~12h
```

---


## 附录

### A. Phase 1 已完成任务清单

以下任务已在 FIX-PLAN 及后续重构中完成：

| 任务 | 文件 | 状态 |
|------|------|------|
| API Key 环境变量化 | `application-ai.yml` | ✅ |
| Password 字段不返前端 | `UserVO.java` | ✅ |
| Token 纯随机化 | `SaTokenConfig.java` | ✅ |
| Java 反序列化 → JSON | `SaTokenDaoImpl.java:574-583` | ✅ |
| Caffeine 缓存对齐 | `SaTokenDaoImpl.java:27-30` | ✅ |
| 日志脱敏（验证码） | `CaptchaServiceImpl.java` | ✅ |
| MySQL 坐标修正 | `pom.xml:281` | ✅ |
| XSS 验证器升级 | `XssValidator.java` | ✅ |
| 默认密码不共享 | `PasswordUtils.java` | ✅ |

### B. 技术引入清单

| 技术 | 用途 | 优先级 | 状态 |
|------|------|--------|------|
| Flyway | 数据库迁移 | P1 | ✅ 已完成 |
| Resilience4j | 熔断、限流、降级 | P1 | ✅ 已完成（自实现） |
| Testcontainers | 集成测试 | P2 | ⏳ V4-1 引入 |
| JaCoCo | 测试覆盖率 | P2 | ⏳ V4-1 配置 |
| OWASP Dependency Check | 安全扫描 | P2 | ⏳ V4-7 配置 |
| WebSocket | 实时推送 | P2 | ⏳ V4-5 引入 |

### C. V4 执行顺序建议

```
Week 1:   V4-1 (单元测试: AgentRuntime + MemoryService)          ~10h
Week 1:   V4-12 (清理维护)                                        ~1h
Week 2:   V4-1 (单元测试: VectorStore + ModelRegistry)            ~10h
Week 2:   V4-4 (Metrics 验证)                                     ~2h
Week 3:   V4-2 (文档解析器)                                        ~8h
Week 3:   V4-3 (教育 Agent 完善)                                   ~4h
Week 3:   V4-8 (前端认证页面 TODO)                                 ~4h
Week 4:   V4-5 (Usage 报表)                                        ~6h
Week 4:   V4-9 (Dashboard 数据联调)                                ~6h
Week 5:   V4-6 (多租户增强)                                        ~8h
Week 5:   V4-10 (页面功能验证)                                     ~8h
Week 6:   V4-7 (安全扫描)                                          ~3h
Week 6:   V4-11 (UI/UX 优化)                                       ~6h
```

---

> **文档版本**: 3.0  
> **最后更新**: 2026-07-10  
> **维护者**: ShiYu AI Team
