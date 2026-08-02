# shiyu-ai 项目架构概览

> 版本: 1.0.0 | Java 21 | Spring Boot 4.1.0 | 最后更新: 2026-08-02

> 当前 Maven 实际构建边界以根 `pom.xml` 为准：Common BOM、Observation 空壳和 Excel 模块已退出默认构建；线程模块仍被 Knowledge Worker 使用；向量模块保持独立领域边界。

---

## 一、项目定位

shiyu-ai 是一个面向**企业级 AI 平台**的后端系统，基于 Spring Boot 4.1 + Java 21 构建。项目采用 Maven 多模块架构，提供 Agent 编排、RAG 知识库、多模型适配、记忆管理、工具调用等 AI 核心能力。

---

## 二、整体架构

### 2.1 模块全景

``
shiyu-ai (POM)
│
├── shiyu-common                  # 公共基础模块（BOM 管理）
│   ├── shiyu-common-core         # 核心工具类（Result, JSON, Mapstruct）
│   ├── shiyu-common-web          # Web 通用配置（异常处理、跨域等）
│   ├── shiyu-common-mybatis      # MyBatis 通用配置
│   ├── shiyu-common-thread       # Worker 线程池配置
│   └── shiyu-common-storage      # 文件、断点上传、备份和存储元数据
│
├── shiyu-ai-dal                  # 统一数据访问层
│   ├── agent/                    # Agent 数据层（DO/BO/Mapper/Repository）
│   ├── auth/                     # 权限认证数据层
│   ├── common/                   # 通用数据层
│   ├── education/                # 教育领域数据层
│   ├── knowledge/                # 知识库数据层
│   ├── memory/                   # 记忆数据层
│   ├── model/                    # 模型数据层
│   └── record/                   # 记录数据层
│
├── shiyu-ai-model                # AI 模型适配层
│   ├── adapter/                  # ModelAdapter SPI（GenericPlatform / Ollama）
│   ├── chat/                     # ChatEngine 对话引擎
│   ├── embedding/                # EmbeddingService 向量化服务
│   ├── config/                   # 平台配置（PlatformProperties）
│   ├── controller/               # 模型管理 API
│   ├── event/                    # 模型调用事件
│   ├── resilience/               # 弹性能力（熔断/限流/负载均衡）
│   └── service/                  # 模型/平台管理服务
│
├── shiyu-ai-memory               # 记忆管理模块
│   ├── chat/                     # 对话记忆提供者
│   ├── compressor/               # 记忆压缩（滑动窗口）
│   ├── config/                   # 自动化配置
│   ├── impl/                     # MemoryService 实现
│   ├── pipeline/                 # 记忆整合管道
│   ├── recall/                   # 混合召回策略
│   ├── request/                  # 请求 DTO
│   ├── service/                  # 摘要是生成服务
│   └── spi/                      # MemoryStore SPI 接口
│
├── shiyu-ai-knowledge            # 知识库/RAG 模块
│   ├── config/                   # 索引初始化
│   ├── controller/               # 知识管理 API
│   ├── document/                 # 文档解析器（PDF/Word/Markdown）
│   ├── domain/                   # 知识图谱领域模型
│   ├── dto/                      # 数据传输对象
│   ├── graph/                    # 知识图谱存储
│   ├── path/                     # 学习路径服务
│   ├── rag/                      # RAG 核心（分块/检索/重排/编排）
│   ├── request/                  # 请求 DTO
│   ├── search/                   # 搜索服务
│   ├── service/                  # 知识库管理服务
│   └── task/                     # 索引重建定时任务
│
├── shiyu-ai-agent                # Agent 核心模块
│   ├── builder/                  # Agent 构建器
│   ├── cache/                    # Agent 缓存管理人
│   ├── checkpoint/               # 检查点（暂停/恢复）
│   ├── config/                   # WebMVC/拦截器/启动配置
│   ├── controller/               # Agent 定义/版本/执行 API
│   ├── education/                # 教育领域 Agent（考试/辅导/报告）
│   ├── event/                    # Agent 执行事件体系
│   ├── execution/                # 执行实例/状态管理
│   ├── graph/                    # 图编排引擎（LangGraph4j）
│   ├── lifecycle/                # Agent 状态机（CREATED→DEPLOYED→ARCHIVED）
│   ├── node/                     # 节点系统（LLM/RAG/Memory/Tool 等 15+ 节点）
│   ├── request/                  # 请求 DTO
│   ├── retry/                    # 重试策略
│   ├── runtime/                  # Agent 运行时（AgentRuntime / AgentExecutor）
│   ├── service/                  # Agent 定义/版本/审计管理
│   ├── timeout/                  # 超时策略
│   ├── vo/                       # 视图对象
│   └── workflow/                 # 工作流编排
│       ├── component/            # 教育编排组件
│       └── context/              # 上下文对象（Ability/Learning/Recommend/Tutor）
│
├── shiyu-ai-tool                 # 工具调用模块
│   ├── config/                   # MCP 自动化配置
│   └── mcp/                      # MCP 工具注册/描述/调用
│
├── shiyu-ai-vector               # 向量存储模块
│   ├── VectorStore*              # 公共存储与隔离空间 Provider 接口
│   ├── config/                   # 默认后端配置
│   ├── factory/                  # Provider 与内部实现工厂
│   └── impl/                     # InMemory / JVector 实现
│
├── shiyu-ai-auth                 # 认证授权模块
├── shiyu-ai-record               # 记录存储模块
├── shiyu-ai-plugin               # 插件模块
├── shiyu-ai-usage                # 用量统计（待完善）
├── shiyu-ai-education            # 教育领域模块
├── shiyu-ai-web                  # Controller、DTO 和 Web 适配层
└── shiyu-ai-bootstrap            # 应用启动入口
``

### 2.2 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 4.1.0 |
| AI 框架 | Spring AI + LangChain4j | 2.0.0 / 1.16.3 |
| 图编排 | LangGraph4j | 1.8.19 |
| ORM | MyBatis-Flex + MyBatis-Plus | 1.11.7 / 3.5.16 |
| 认证 | Sa-Token | 1.45.0 |
| 向量数据库 | JVector + 自研 InMemory | 4.0.0-beta.6 |
| 数据库 | H2 / MySQL | 2.4.240 / 9.4.0 |
| 响应式 | Project Reactor | Spring Boot 内置 |
| API 文档 | SpringDoc OpenAPI（UI 可选） | 3.0.2 |
| 映射工具 | MapStruct Plus | 1.5.0 |
| 工具 | Hutool / Guava / Lombok | — |
| 编排引擎 | LiteFlow | 2.16.0 |
| 构建 | Maven (flatten 版本管理) | — |

---

## 三、核心架构模式

### 3.1 分层架构

``
Controller → Application (待完善) → Domain (Service + Domain Model) → Infrastructure (Repository/Mapper/DB)
                                                                       ↓
                                                                  Event Publisher
                                                                       ↓
                                                                  Event Listeners
``

### 3.2 数据流转

``
Request (VO/DTO) → BO (Business Object) → DO (Data Object) → DB
                   ↑                        ↓
              Repository ←────────────── Mapper (MyBatis-Flex)
``

### 3.3 AI 执行流程

``
用户请求 → ExecutionController → AgentRuntime → AgentExecutor 
    → Graph.compile() → CompiledGraph.invoke(input)
        → Node1 → Node2 → ... → NodeN 
    → Execution 结果 → 持久化 + 事件发布 → 响应
``

### 3.4 事件驱动架构

``
AgentRuntime (事件源)
    ↓
EventPublisher.publish(DomainEvent)
    ↓
Spring ApplicationEventPublisher
    ↓
AgentEventListener / AuditEventListener / TimelineEventListener
    ↓
持久化 / 审计 / 时间线 / WebSocket / 通知
``

---

## 四、关键设计亮点

### 4.1 Node 插件化设计（NodeFactory + NodeCreator SPI）

通过双路径策略创建节点：
- **路径 A（优先）**：@Component implements NodeCreator 的 Spring Bean，支持 DI
- **路径 B（fallback）**：工厂内部注册的 lambda 表达式，用于无 DI 的简单节点

新增节点类型只需：
1. 创建 NodeConfig → Node 实现
2. 注册 @Component NodeCreator 或通过 egisterNodeType()
3. 在 NodeType 枚举中新增条目

符合**开闭原则**，无需修改 NodeFactory。

### 4.2 Repository 层统一抽象

每个业务域统一使用：
``
BO (业务对象) → Repository (接口) → Mapper (MyBatis-Flex) → DO (数据对象) → DB
``

Repository 层确保业务层与具体数据库实现解耦，便于切换数据库或增加缓存。

### 4.3 状态机验证

AgentStateMachine 严格验证 ExecutionStatus 的合法转换，避免非法状态跃迁。

### 4.4 检查点机制

CheckpointManager + DbCheckpointStore 支持 Agent 执行的暂停/恢复，通过保存节点执行状态的快照实现断点续执行。

---

## 五、评分汇总

| 维度 | 评分 | 说明 |
|------|:---:|------|
| Maven 多模块设计 | 10/10 | 模块边界清晰，可扩展性好 |
| 公共基础模块 | 9.5/10 | common-* 拆分合理 |
| Spring Boot 工程规范 | 9/10 | 基础规范完善，待引入 Applicaton 层 |
| AI 模型适配层 | 9/10 | 已有 Adapter，待引入 Capability |
| Agent 架构 | 7.5/10 | 生命周期/状态管理待增强 |
| RAG/Knowledge | 8/10 | 功能完整，职责待进一步拆分 |
| Repository 抽象 | 7/10 | 已有 Repository，耦合度可控 |
| 事件驱动 | 6.5/10 | 基础具备，需异步化 + 事件补全 |
| Runtime | 5/10 | 缺少全局统一运行时 |
| 可扩展性 | 8/10 | 企业级后台优秀，AI 平台待演进 |
| **总体** | **8.8/10** | 基础扎实，演进路径清晰 |

---

## 六、相关文档

- [模块详解](./modules/模块详解.md)
- [架构评审报告](./architecture/架构评审报告.md)
- [演进路线图](./guides/演进路线图.md)
