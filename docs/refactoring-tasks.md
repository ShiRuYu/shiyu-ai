# ShiYu AI 重构任务清单

> **文档版本**: 3.0  
> **创建日期**: 2026-07-10  
> **最后更新**: 2026-07-10  
> **基于**: Architecture Design Document v2.1  
> **状态**: ✅ Phase 1 (P0/P1/P2/V3) 全部完成 → 🚀 V4 进行中

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

- [ ] 2.1 实现 PdfDocumentParser
  - [ ] 添加 PDFBox 依赖（pom.xml）
  - [ ] 实现 PDF 文本提取
  - [ ] 支持带格式的文本段落提取
  - [ ] 单元测试（多种 PDF 样本文档）
- [ ] 2.2 实现 WordDocumentParser
  - [ ] 添加 Apache POI 依赖（pom.xml）
  - [ ] 实现 .docx 文本提取
  - [ ] 支持表格/标题/段落结构化提取
  - [ ] 单元测试（多种 Word 样本文档）
- [ ] 2.3 文档解析器集成测试
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

- [ ] 3.1 ReviewAgent 完善
  - [ ] 完成艾宾浩斯复习计划的自动生成逻辑
  - [ ] 对接复习计划数据库表
  - [ ] 补充单元测试
- [ ] 3.2 IntentDefApplicationRunner 完善
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

- [ ] 4.1 AgentMetrics 验证
  - [ ] `agent.execution.total` Counter 正确递增
  - [ ] `agent.execution.duration` Timer 正确记录
- [ ] 4.2 ModelMetrics 验证
  - [ ] `model.call.total` Counter 正确递增
  - [ ] `model.call.duration` Timer 正确记录
  - [ ] `model.token.total` Counter 正确记录 Token 数
- [ ] 4.3 KnowledgeMetrics 验证
  - [ ] `knowledge.retrieval.total` Counter 正确递增
  - [ ] `knowledge.retrieval.duration` Timer 正确记录
- [ ] 4.4 MemoryMetrics 验证
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

- [ ] 5.1 聚合报表
  - [ ] 按天聚合统计 API
  - [ ] 按周聚合统计 API
  - [ ] 按月聚合统计 API
  - [ ] 按模型/平台维度分组统计
- [ ] 5.2 WebSocket 实时推送
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

- [ ] 6.1 资源配额模型
  - [ ] 定义租户配额实体（Token 限额 / Agent 数量 / 存储空间）
  - [ ] 创建数据库表
- [ ] 6.2 配额校验拦截
  - [ ] Agent 创建时校验配额
  - [ ] Token 消耗时校验配额
  - [ ] 超配额时返回明确错误信息
- [ ] 6.3 管理 API
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

- [ ] 7.1 配置 OWASP Dependency Check
  - [ ] 在根 pom.xml 添加 Maven 插件
  - [ ] 配置扫描范围（全部依赖）
- [ ] 7.2 首次扫描与修复
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

- [ ] 8.1 register.vue — 对接注册接口 `POST /api/auth/register`
- [ ] 8.2 code-login.vue — 对接验证码登录接口 `POST /api/auth/code-login`
- [ ] 8.3 forget-password.vue — 对接忘记密码接口 `POST /api/auth/forget-password`
- [ ] 8.4 如有必要，补充后端对应 API 接口

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

- [ ] 9.1 分析页数据联调
  - [ ] 对接 Usage Center 统计 API（Token 用量 / Cost / 请求量）
  - [ ] 对接 Agent 执行统计 API（执行次数 / 成功率 / 平均耗时）
- [ ] 9.2 工作空间页数据联调
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

- [ ] 10.1 教育管理页面验证
  - [ ] 科目 / 教材 / 章节 / 知识点 CRUD
  - [ ] 试题 / 考试 / 复习计划 / 错题本
  - [ ] 学生管理 / 资源管理
- [ ] 10.2 AI 对话 SSE 流式渲染优化
  - [ ] SSE 断线重连机制
  - [ ] 对话历史管理（加载/清除）
  - [ ] 流式渲染性能优化
- [ ] 10.3 知识图谱页面交互优化
  - [ ] 复杂关系展示优化（父子/前后置/相关）
  - [ ] 节点拖拽/缩放交互
- [ ] 10.4 其他页面快速验证
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

- [ ] 11.1 统一 shiyu 品牌色彩和 Logo
  - [ ] 替换 Vben Admin 默认品牌色为 shiyu 品牌色
  - [ ] 设置站点的 Logo 和 favicon
- [ ] 11.2 补充缺漏的国际化 i18n 翻译
  - [ ] 扫描所有视图页面的 i18n key
  - [ ] 补充缺失的翻译条目
- [ ] 11.3 移动端响应式适配
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
