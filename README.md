# ShiYu AI · 拾羽 AI

> 基于 Java 21 + Spring Boot 4.x 的企业级 AI 服务平台 — 图编排 Agent、RAG 知识引擎、多平台 LLM 适配、MCP 工具集成、智能教育辅导

---

## 目录

- [项目简介](#项目简介)
- [架构总览](#架构总览)
- [模块说明](#模块说明)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [配置指南](#配置指南)
- [API 文档](#api-文档)
- [开发规范](#开发规范)
- [项目文档](#项目文档)
- [License](#license)

---

## 项目简介

**ShiYu AI（拾羽 AI）** 是一个面向 AI 教育场景的企业级智能平台，采用模块化单体架构（Modular Monolith），覆盖 AI 对话、Agent 编排、知识库 RAG、智能教育辅导等核心功能。

核心特性：

- **图编排 Agent** — 基于 `langgraph4j` 的状态图引擎，支持 13 种可编排节点类型
- **LiteFlow 工作流** — 规则引擎编排聊天流程，支持 Direct / CoT / ToT 策略
- **RAG 知识引擎** — 文档解析 + 智能分块 + JVector HNSW 向量检索 + 知识图谱增强
- **多平台 LLM 适配** — 统一接口对接 OpenAI、Ollama、DeepSeek、硅基流动、OpenRouter
- **MCP 协议集成** — 基于 Spring AI MCP 的工具服务体系
- **多租户 RBAC** — Sa-Token 认证 + 租户/工作空间/角色/菜单权限体系
- **教育领域** — 布鲁姆认知分类、艾宾浩斯遗忘曲线复习、智能组卷、学情分析
- **可观测性** — OpenTelemetry + Micrometer + Prometheus 全链路追踪

---

## 架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                        shiyu-ai-bootstrap (:9000)                │
├────────┬────────┬──────────┬──────────┬──────────┬──────────────┤
│  auth  │ agent  │ education│ knowledge│  record  │    core      │
│ 认证权限│ Agent  │ 教育领域  │ 知识引擎  │ 记录管理  │  AI 核心    │
│        │ 编排   │          │  RAG     │          │ Chat/Model   │
├────────┴────────┴──────────┴──────────┴──────────┴──────────────┤
│                         shiyu-ai-dal (数据访问层)                 │
│              DO / BO / Mapper / Repository                       │
├─────────────────────────────────────────────────────────────────┤
│                       shiyu-common (公共基础)                     │
│     core │ web │ mybatis │ thread │ excel │ bom                  │
└─────────────────────────────────────────────────────────────────┘
```

### Agent 图编排流程

```
用户输入 → AgentDefinition → Graph 编译 → StateGraph 执行 →
  意图识别(INTENT) → 条件分支(CONDITION) →
  LLM调用 / 工具调用 / RAG检索 / 记忆读写 / 输出格式化
```

### RAG 检索流程

```
用户查询 → Embedding 向量化 → JVector HNSW 检索 →
  知识图谱上下文增强 → 上下文拼接 → LLM 生成回答
```

---

## 模块说明

### shiyu-ai-agent — Agent 编排引擎

基于 `langgraph4j` 的自定义 Agent 状态图引擎，支持 13 种可编排节点：

| 节点类型 | 用途 |
|----------|------|
| `INTENT` | 用户意图识别 |
| `LLM_CALL` | LLM 模型调用 |
| `TOOL_CALL` | 工具函数调用 |
| `RAG_RETRIEVAL` | 知识库检索 |
| `RAG_ENHANCEMENT` | 检索后增强 |
| `SHORT_TERM_MEMORY` | 短期记忆读写 |
| `LONG_TERM_MEMORY` | 长期记忆读写 |
| `MEMORY_RETRIEVAL` | 跨会话记忆检索 |
| `CONDITION` | 条件分支路由 |
| `AGENT_CALL` | 子 Agent 调用 |
| `TRANSFORM` | 数据转换 |
| `OUTPUT_FORMAT` | 输出格式化 |
| `DEFAULT` | 默认节点 |

核心能力：
- **动态 Agent 注册** — 运行时注册/注销 Agent 定义
- **版本管理** — 支持多版本共存与热切换（DRAFT / PUBLISHED / ARCHIVED）
- **同步/流式执行** — `POST /api/agent/{agentId}/execute` + SSE 流式端点
- **节点级重试与超时** — 每个节点独立配置重试策略和超时时间
- **LiteFlow 工作流** — 教育场景复杂流程编排（19 个流程组件）

### shiyu-ai-core — AI 核心

AI 对话引擎与模型管理：

- **ChatEngine** — 统一对话接口，支持同步/流式 + 带记忆对话
- **ModelManager** — 多平台模型适配器管理（启动时从 DB 加载，支持热更新）
- **MemoryService** — 短期记忆（对话历史）+ 长期记忆（持久化 + 重要性衰减）
- **ToolService** — MCP 工具调用服务
- **EmbeddingService** — 基于 BGE-small-zh ONNX 的本地嵌入模型

### shiyu-ai-knowledge — 知识引擎

RAG 检索增强生成与知识图谱：

- **文档管理** — 知识点的 CRUD + 关系管理（前置/后续/包含/相关/相似/归属）
- **向量检索** — 基于 JVector（纯 Java HNSW）的向量存储，支持磁盘持久化
- **知识图谱** — 图结构存储知识点关系，支持父子/前后/相关查询
- **RAG 编排** — 向量检索 + 图谱上下文增强 → 拼接上下文 → LLM 生成
- **中文分块** — 针对中文优化的文档分块策略
- **索引重建** — 支持异步全量重建向量索引

### shiyu-ai-education — 智能教育

面向 K12 教育场景的 AI 辅导系统：

- **知识体系** — 教材/章节/知识点三级结构，支持多版本教材
- **能力评估** — 布鲁姆认知分类六维度（记忆/理解/应用/分析/评价/创造）
- **智能组卷** — AI 根据薄弱知识点自动生成试卷
- **复习规划** — 艾宾浩斯遗忘曲线驱动的间隔复习（6 轮复习计划）
- **学情分析** — 能力雷达图、学习趋势、薄弱知识点分析
- **学习路径** — 基于知识图谱的个性化学习路径推荐

### shiyu-ai-auth — 认证授权

企业级 RBAC 权限体系：

- **Sa-Token** — 轻量级认证框架，支持登录/权限/会话管理
- **多租户** — 租户 → 工作空间 → 用户 三级隔离
- **权限模型** — 用户 / 角色 / 菜单 / 工作空间 / 权限码
- **安全防护** — XSS 过滤、验证码、登录限流、密码加密

### shiyu-ai-record — 记录管理

个人记录与时间线：

- **人物档案** — 档案管理与成员关联
- **时间线** — 事件时间线记录
- **多媒体** — 图片/视频/音频附件管理
- **标签系统** — 灵活的标签分类

### shiyu-ai-dal — 数据访问层

统一的数据访问抽象：

- **DO/BO 分离** — 数据对象（DO）与业务对象（BO）分层
- **Repository 模式** — 封装 MyBatis-Flex Mapper，对外返回 BO
- **多租户支持** — 自动注入 `tenant_id` 过滤条件
- **H2/MySQL 双模式** — 开发环境 H2 文件模式，生产环境 MySQL

### shiyu-common — 公共基础

| 子模块 | 功能 |
|--------|------|
| `core` | 统一返回 `Result`、分页查询、异常体系、工具类、事务钩子、事件机制 |
| `web` | XSS 过滤、请求流重复读取、OpenAPI 文档、资源拦截器 |
| `mybatis` | MyBatis-Flex 封装、`TenantEntity`、P6Spy SQL 日志 |
| `thread` | 线程池管理、虚拟线程工厂、OpenTelemetry 上下文透传、Micrometer 指标 |
| `excel` | Excel 导入导出、字典转换、枚举转换、单元格合并策略 |
| `bom` | Maven BOM 统一版本声明 |

---

## 技术栈

| 领域 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 4.1.0 |
| AI 框架 | Spring AI | 2.0.0 |
| LLM 适配 | LangChain4j | 1.16.3 |
| Agent 引擎 | LangGraph4j | 1.8.19 |
| 流程编排 | LiteFlow | 2.16.0 |
| 认证授权 | Sa-Token | 1.45.0 |
| ORM | MyBatis-Flex | 1.11.7 |
| 数据库 | H2（开发）/ MySQL（生产） | 2.4.240 / 9.4.0 |
| 连接池 | Druid | 1.2.27 |
| 向量检索 | JVector（HNSW） | 4.0.0-beta.6 |
| 嵌入模型 | BGE-small-zh（ONNX 本地） | — |
| 缓存 | Caffeine | 3.2.3 |
| 对象映射 | MapStruct-Plus + Lombok | 1.5.0 / 1.18.42 |
| 工具库 | Hutool / Guava / Commons | 5.8.43 / 33.5.0 / 3.20.0 |
| API 文档 | SpringDoc OpenAPI + Knife4j | 3.0.2 / 4.5.0 |
| 可观测性 | OpenTelemetry + Micrometer + Prometheus | — |
| 响应式 | Reactor (Flux) | — |
| 日志 | Log4j2 | — |
| 调度 | XXL-Job | 3.3.2 |
| 对象存储 | AWS S3 SDK | 2.41.18 |
| 构建 | Maven | 3.8+ |

---

## 快速开始

### 环境要求

- **JDK 21+**（项目使用 Java 21 虚拟线程等特性）
- **Maven 3.8+**
- **Git**

### 克隆 & 构建

```bash
git clone https://github.com/ShiRuYu/shiyu-ai.git
cd shiyu-ai
mvn clean install -DskipTests
```

### 配置 AI 平台

编辑配置文件 `shiyu-ai-core/src/main/resources/config/config.yml`：

```yaml
shiyu:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
    openai:
      base-url: https://api.openai.com/v1
      api-key: ${AI_OPENAI_API_KEY:}
    siliconflow:
      base-url: https://api.siliconflow.cn
      api-key: sk-xxxxxxxxxxxxxxxx
      model: THUDM/GLM-Z1-9B-0414
    deepseek:
      base-url: https://api.deepseek.com
      api-key: sk-xxxxxxxxxxxxxxxx
      model: deepseek-chat
```

### 启动应用

项目为单体应用，通过 `shiyu-ai-bootstrap` 统一启动：

```bash
cd shiyu-ai-bootstrap
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

启动后访问：
- 应用端口：`http://localhost:9000`
- API 文档：`http://localhost:9000/doc.html`

### Maven Profile

| Profile | 用途 | 默认 |
|---------|------|------|
| `dev` | 开发环境（trace 日志，H2 数据库） | ✅ |
| `prod` | 生产环境（warn 日志，MySQL 数据库） | |

---

## 配置指南

### 应用配置

```yaml
server:
  port: 9000

shiyu:
  ai:
    # 模型平台配置（也可通过数据库管理）
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
    siliconflow:
      base-url: https://api.siliconflow.cn
      api-key: sk-xxx
      model: THUDM/GLM-Z1-9B-0414
  memory:
    enabled: true
    max-short-term-memories: 10
    max-long-term-memories: 50
  vector:
    type: hnsw          # hnsw / inmemory
    dimension: 512
    data-dir: ${app.home}/data/vector
```

### 环境变量

| 变量 | 说明 |
|------|------|
| `AI_OPENAI_API_KEY` | OpenAI API Key |
| `APP_HOME` | 应用数据目录（H2 数据库、向量索引存储位置） |

---

## API 文档

启动应用后访问：

```
http://localhost:9000/doc.html          # Knife4j 文档
http://localhost:9000/swagger-ui/index.html  # Swagger UI
http://localhost:9000/v3/api-docs       # OpenAPI 3.0 JSON
```

API 按模块分组：

| 分组 | 路径前缀 |
|------|----------|
| Agent | `/api/agent/**` |
| 认证 | `/api/auth/**` |
| 知识库 | `/api/knowledge/**` |
| 教育 | `/api/education/**` |
| 记录 | `/api/record/**` |
| 系统 | `/api/system/**` |

---

## 开发规范

- **Lombok** — `@Data`、`@Slf4j`、`@RequiredArgsConstructor` 简化代码
- **MapStruct-Plus** — BO <-> VO 对象映射
- **Jakarta Validation** — `@Valid` 参数校验分组（AddGroup / EditGroup / QueryGroup）
- **XSS 过滤** — 全局 XSS 过滤器（Jsoup Safelist）防止跨站脚本攻击
- **统一异常** — `@ControllerAdvice` + 业务异常体系 + 统一 `Result<T>` 返回
- **分页查询** — `PageQuery` + `PageData<T>` 统一分页模型
- **OpenTelemetry** — 线程池上下文透传 + 调用链追踪
- **多租户** — 所有业务表包含 `tenant_id` + `workspace_id`，自动过滤

### 项目分层

```
controller/   <- REST 接口层（Request -> VO）
service/      <- 业务逻辑层（BO）
  impl/       <- 实现类
repository/   <- 仓储层（返回 BO）
mapper/       <- MyBatis 映射接口（操作 DO）
dal/
  dataobject/ <- 数据对象（DO，映射数据库行）
  bo/         <- 业务对象（BO，Repository 对外返回）
  mapper/     <- 数据访问映射
  repository/ <- 仓储实现
config/       <- 配置类
```

---

## 项目文档

| 文档 | 说明 |
|------|------|
| [架构设计文档 (ADD)](./docs/architecture/shiyu-ai-architecture-design.md) | 企业级架构设计，含 20 章完整内容 |
| [重构任务清单](./docs/refactoring-tasks.md) | 21 项重构任务，含子任务 Checklist |

---

## License

本项目采用 MIT 许可证。详情请查看 [LICENSE](./LICENSE) 文件。
