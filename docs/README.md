# shiyu-ai 项目架构概览

> 版本: 1.0.0 | Java 21 | Spring Boot 4.1.0 | 最后更新: 2026-08-02

> 当前 Maven 实际构建边界以根 `pom.xml` 为准：Common BOM、Observation 空壳和 Excel 模块已退出默认构建；线程模块仍被 Knowledge Worker 使用；向量模块保持独立领域边界。

---

## 一、项目定位

shiyu-ai 是一个面向**企业级 AI 平台**的后端系统，基于 Spring Boot 4.1 + Java 21 构建。项目采用 Maven 多模块架构，提供 Agent 编排、RAG 知识库、多模型适配、记忆管理、工具调用等 AI 核心能力。

---

## 二、整体架构

### 2.1 模块全景

```
shiyu-ai (POM)
│
├── 🎯 业务层（面向场景的产品能力）
│   ├── shiyu-ai-education        # 智能教育：学练测评荐 + 教育专用 Agent/节点
│   │   ├── agent/                # 教育 Agent（Exam/Planner/Report/Review）+ 业务节点
│   │   ├── domain/               # 领域模型（布鲁姆分类/艾宾浩斯曲线）+ BO
│   │   ├── dto/                  # 业务响应模型
│   │   ├── port/repository/      # 仓储接口（依赖倒置）
│   │   ├── service/              # 业务服务
│   │   └── storage/              # 教育资源种子
│   └── shiyu-ai-record           # 记录管理：档案/时间线/媒体/标签
│       ├── domain/  port/  service/  request/  vo/
│
├── ⚙️ 平台层（可复用的 AI 能力）
│   ├── shiyu-ai-agent            # ⭐ 平台核心：Agent 图编排引擎
│   │   ├── builder/ cache/ checkpoint/ graph/ node/ runtime/
│   │   ├── execution/ lifecycle/ retry/ timeout/ event/
│   │   ├── service/ vo/ request/
│   │   ├── domain/model/         # BO
│   │   └── port/repository/      # 8 个仓储接口
│   ├── shiyu-ai-auth             # 认证授权：Sa-Token / 多租户 RBAC
│   ├── shiyu-ai-model            # 模型管理：适配器/热更新/弹性/嵌入
│   ├── shiyu-ai-knowledge        # 知识引擎：文档/RAG/图谱/检索/审计/评测/上传
│   ├── shiyu-ai-vector           # 向量存储：JVector HNSW + Provider API
│   ├── shiyu-ai-memory           # 记忆系统：短期/长期/跨会话
│   ├── shiyu-ai-tool             # MCP 工具体系
│   ├── shiyu-ai-usage            # 用量计量：Token/实时推送
│   └── shiyu-ai-plugin           # 插件体系：生命周期/沙箱/热插拔
│
├── 🧱 基础设施层（纯技术底座）
│   ├── shiyu-common              # 公共基础
│   │   ├── shiyu-common-core     # 工具/Result/异常/事务
│   │   ├── shiyu-common-web      # XSS/请求包装/拦截器
│   │   ├── shiyu-common-mybatis  # ORM 封装/租户/审计字段
│   │   ├── shiyu-common-thread   # 线程池/Otel 链路
│   │   ├── shiyu-common-excel    # Excel 导入导出
│   │   └── shiyu-common-storage  # 文件存储/断点续传/备份
│   ├── shiyu-ai-dal              # 数据访问实现层：DO/Mapper/Repo 实现 + Flyway
│   ├── shiyu-ai-web              # REST 接入层：Controller/DTO/WebSocket/OpenAPI
│   └── shiyu-ai-bootstrap        # 启动入口：日志/可观测/数据保留
│
└── 📦 支撑：docs/ scripts/ plugins/ data/
```

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
| Agent 流程图 | LangGraph4j + BaseNode | — |
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
2. 注册 @Component NodeCreator 或通过 
egisterNodeType()
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

## 五、评分汇总（2026-08-04 更新）

| 维度 | 评分 | 说明 |
|------|:---:|------|
| Maven 多模块设计 | 10/10 | 15 个有效模块，边界清晰，依赖单向向下 |
| 公共基础模块 | 9.5/10 | common-* 拆分合理（core/web/mybatis/thread/storage） |
| DDD 分层落地 | 9.5/10 | domain/model + port/repository + dal 实现，LayerBoundaryArchTest 强制边界 |
| Spring Boot 工程规范 | 9/10 | 基础规范完善，可继续引入 Application 层 |
| AI 模型适配层 | 9/10 | Chat/Embedding 分离，多模态 Capability 待引入 |
| Agent 架构 | 9/10 | 图编排完整；教育节点已解耦至 education |
| RAG/Knowledge | 9/10 | 统一检索（retrieval/）+ 空间授权 + 审计/评测已落地 |
| Repository 抽象 | 9/10 | 接口（port）+ 实现（dal）倒置，可加泛型实现基类 |
| 事件驱动 | 7.5/10 | 事件体系健全，待异步化 + 细粒度事件 |
| Runtime | 9/10 | 决策取消独立 runtime，Agent 引擎统一编排更简洁 |
| 教育/业务解耦 | 9.5/10 | 教育 Agent 迁入 education，平台不依赖业务 |
| 可扩展性 | 9/10 | 平台 + 插件化业务方向清晰 |
| **总体** | **9.2/10** | 架构收敛，演进路径清晰 |

---

## 六、相关文档

- [模块详解](./modules/模块详解.md)
- [架构评审报告](./architecture/架构评审报告.md)
- [演进路线图](./guides/演进路线图.md)
- [知识平台迁移执行计划](./architecture/知识平台迁移执行计划.md)
- [知识平台与教育模块调整方案](./architecture/知识平台与教育模块调整方案.md)
