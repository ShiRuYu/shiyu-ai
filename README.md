# ShiYu AI · 拾羽 AI

> **多平台可接入的 AI 智能体平台** — 以自定义 Agent 编排引擎为基石，快速构建业务智能体。
> 当前已扩展：**Record（记录管理）** 与 **Education（智能教育）** 两大业务方向。

---

## 项目定位

拾羽 AI（ShiYu AI）不是一个单一功能的 AI 应用，而是一个**可接入多 LLM 平台、以自定义 Agent 为核心、向多个业务方向扩展**的 AI 智能体平台。

当前根 Maven reactor 包含 19 个实际构建模块。版本管理已收敛到根 POM；Observation 空壳和 Excel 模块不再进入默认构建，Thread 模块作为 Knowledge Worker 的基础设施继续保留。

```
                     ┌──────────────────┐
                     │  多 LLM 平台接入    │
                     │ OpenAI / DeepSeek  │
                     │  Ollama / Silicon  │
                     │   Flow / 更多...   │
                     └────────┬─────────┘
                              │ 统一适配层
                     ┌────────▼─────────┐
                     │    Agent 引擎     │ ◄── 平台核心
                     │ 图编排 · 13 种节点 │
                     │ 记忆 · 工具 · RAG │
                     └────────┬─────────┘
                              │
              ┌───────────────┼───────────────────┐
              │               │                   │
     ┌────────▼──────┐  ┌────▼────┐      ┌──────▼──────┐
     │  Record       │  │Education│      │   更多...     │
     │  记录管理      │  │ 智能教育  │      │  待扩展...    │
     │  时间线·媒体   │  │ 学练测评  │      │              │
     └───────────────┘  └─────────┘      └─────────────┘

            ▲  平台基础设施：知识库 · 向量存储 · 用量计量
            │  记忆系统 · 插件 · 工具 · MCP · 模型管理
            └────────────────────────────────────────┘
```

---

## 平台基础设施

平台在 Agent 引擎之下，提供了一整套基础设施能力，为 Agent 节点和业务扩展赋能。

### 知识引擎 — Knowledge Engine (`shiyu-ai-knowledge`)

涵盖文档管理、RAG 检索、知识图谱的一站式知识服务：

- **文档管理** — 知识点的 CRUD + 关系管理（前置/后续/包含/相关/相似/归属）
- **向量检索** — 通过 vector 模块统一接口完成空间隔离的 HNSW 检索
- **知识图谱** — 图结构存储知识点关系，支持父子/前后/相关查询
- **RAG 编排** — 向量检索 → 图谱上下文增强 → 拼接上下文 → LLM 生成
- **中文分块** — 针对中文优化的文档分块策略
- **索引重建** — 支持异步全量重建向量索引

### 向量存储 — Vector Store (`shiyu-ai-vector`)

提供与具体后端解耦的 `VectorStore` / `VectorStoreProvider` 公共接口，默认使用 **JVector（纯 Java HNSW）**，测试和轻量场景可使用 InMemory：

- **HNSW 索引** — 高效的近似最近邻搜索算法
- **磁盘持久化** — 向量索引持久化到磁盘，搜索重启不丢失
- **维度配置** — 支持动态配置向量维度（默认 512 维）
- **多种搜索策略** — 精确搜索 + 近似搜索
- **CRUD 操作** — 向量的增删改查完全覆盖
- **空间隔离** — 按租户、知识空间和索引版本打开独立命名空间
- **统一边界** — Knowledge 与 Memory 仅依赖公共接口，不直接实例化 JVector
- **可替换后端** — 后续接入 ChromaDB、Milvus 时新增 Provider 适配器即可

### 记忆系统 — Memory System (`shiyu-ai-memory`)

支持两级记忆的智能记忆服务：

- **短期记忆** — 对话上下文管理，自动裁剪，保持会话连贯性
- **长期记忆** — 持久化存储 + 重要性衰减机制，提取关键信息长期保留
- **跨会话检索** — 跨不同会话检索相关历史记忆
- **压缩策略** — 智能压缩长对话历史（摘要/裁剪）
- **SPI 扩展** — 支持自定义记忆存储后端

### 工具体系 — Tool & MCP (`shiyu-ai-tool`)

基于 **Spring AI MCP 协议**的标准化工具调用服务：

- **MCP 协议集成** — 标准化的工具描述与调用协议
- **工具注册** — 运行时注册/注销/更新工具定义
- **工具调用执行** — 安全的工具沙箱执行环境
- **动态发现** — 自动发现并注册 MCP 服务器提供的工具
- **与 Agent 打通** — Agent 的 `TOOL_CALL` 节点直接调用注册的工具

### 模型管理 — Model Management (`shiyu-ai-model`)

统一的多平台 LLM 模型适配与管理：

- **多平台适配** — OpenAI / DeepSeek / Ollama / SiliconFlow / OpenRouter 等
- **热更新** — 运行中动态切换模型平台，零停机
- **模型路由** — 支持按场景/租户路由到不同模型
- **弹性容错** — 熔断降级、退避重试，保障服务可用性
- **嵌入模型** — 内置 BGE-small-zh ONNX 本地嵌入模型，无外部依赖

### 用量计量 — Usage Tracking (`shiyu-ai-usage`)

全链路用量与计费跟踪：

- **Token 计量** — 输入/输出 Token 精确统计
- **请求记录** — 每次 Agent / LLM / 工具调用的完整记录
- **实时推送** — WebSocket 实时推送用量数据
- **多维统计** — 按用户、租户、模型、时间段聚合

### 插件体系 — Plugin System (`shiyu-ai-plugin`)

轻量级插件扩展框架：

- **插件生命周期** — 加载 → 启用 → 停用 → 卸载 全生命周期管理
- **沙箱隔离** — 插件沙箱执行，保障平台安全
- **SPI 注册** — 基于 SPI 的插件发现与注册
- **动态热插拔** — 运行时安装/卸载插件，无需重启

---

## 核心架构

### 平台接入层 — 连接多 LLM 平台

通过统一的 ModelManager 适配器机制，一套 API 对接主流 LLM 平台：

| 平台 | 接入方式 |
|------|---------|
| **OpenAI** | 标准 OpenAI API |
| **DeepSeek** | DeepSeek API |
| **Ollama** | 本地私有化部署 |
| **硅基流动 (SiliconFlow)** | 国内加速访问 |
| **OpenRouter** | 多模型路由 |
| **更多...** | 可扩展适配器 |

平台配置支持**启动时加载**和**运行时热更新**，零停机切换模型。

### Agent 引擎 — 图编排自定义智能体

基于 `langgraph4j` 的状态图引擎，支持 13 种可编排节点，像搭积木一样构建 Agent：

| 节点类型 | 用途 |
|----------|------|
| `INTENT` | 用户意图识别，自动路由 |
| `LLM_CALL` | LLM 模型调用 |
| `TOOL_CALL` | MCP 工具函数调用 |
| `RAG_RETRIEVAL` | 知识库检索 |
| `RAG_ENHANCEMENT` | 检索结果增强 |
| `SHORT_TERM_MEMORY` | 短期记忆读写 |
| `LONG_TERM_MEMORY` | 长期记忆读写 |
| `MEMORY_RETRIEVAL` | 跨会话记忆检索 |
| `CONDITION` | 条件分支路由 |
| `AGENT_CALL` | 子 Agent 调用（Agent 嵌套） |
| `TRANSFORM` | 数据转换 |
| `OUTPUT_FORMAT` | 输出格式化 |
| `DEFAULT` | 默认处理节点 |

引擎核心能力：

- **动态注册** — 运行时注册/注销/更新 Agent，无需重启
- **版本管理** — DRAFT / PUBLISHED / ARCHIVED 多版本共存
- **同步/流式执行** — REST 同步调用 + SSE 流式输出
- **节点级重试与超时** — 每个节点独立配置
- **嵌套 Agent** — Agent 内调 Agent，实现复杂任务分解
- **执行生命周期** — PENDING → RUNNING → PAUSED → ... → COMPLETED / FAILED
- **检查点机制** — 节点级执行快照，支持断点暂停恢复

---

## 业务扩展

### 扩展一：Record（记录管理）

一个轻量级的**个人记录与时间线**管理系统，适用于日记、笔记、事件归档等场景。

- **人物档案** — 档案管理，支持成员关联
- **时间线** — 按时间轴记录和展示事件
- **多媒体管理** — 图片 / 视频 / 音频附件的上传与管理
- **标签系统** — 灵活的标签分类与筛选

### 扩展二：Education（智能教育）

面向 K12 教育场景的 **AI 智能辅导**系统，覆盖"学、练、测、评、荐"全链路。

- **知识体系** — 教材 → 章节 → 知识点三级结构，支持多版本教材
- **能力评估** — 基于布鲁姆认知分类（记忆/理解/应用/分析/评价/创造）
- **智能组卷** — AI 根据薄弱知识点自动生成试卷
- **复习规划** — 艾宾浩斯遗忘曲线驱动的 6 轮间隔复习
- **学情分析** — 能力雷达图、学习趋势、薄弱点分析
- **学习路径** — 基于知识图谱的个性化路径推荐

---

## 模块全景

| 模块 | 职责 | 类型 |
|------|------|------|
| `shiyu-ai-agent` | **Agent 引擎**：图编排、节点系统、执行生命周期、检查点、重试/超时 | **平台核心** |
| `shiyu-ai-auth` | 认证授权：Sa-Token、多租户 RBAC | 平台基础 |
| `shiyu-ai-model` | 模型管理：多平台适配器、热更新、熔断降级、嵌入模型 | 平台基础设施 |
| `shiyu-ai-knowledge` | 知识引擎：文档管理、RAG 检索、知识图谱、中文分块 | 平台基础设施 |
| `shiyu-ai-vector` | 向量存储：JVector HNSW 索引、磁盘持久化、CRUD | 平台基础设施 |
| `shiyu-ai-memory` | 记忆系统：短期/长期记忆、压缩策略、跨会话检索、SPI 扩展 | 平台基础设施 |
| `shiyu-ai-tool` | 工具体系：MCP 协议集成、工具注册/调用/执行 | 平台基础设施 |
| `shiyu-ai-plugin` | 插件体系：生命周期管理、沙箱隔离、动态热插拔 | 平台基础设施 |
| `shiyu-ai-usage` | 用量计量：Token 统计、实时推送、多维聚合 | 平台基础设施 |
| `shiyu-ai-record` | **记录管理业务**：人物档案、时间线、多媒体、标签 | **业务扩展** |
| `shiyu-ai-education` | **智能教育业务**：学练测评荐全链路 | **业务扩展** |
| `shiyu-ai-bootstrap` | 应用启动入口 | 基础设施 |
| `shiyu-ai-dal` | 数据访问层：DO/BO/Repository 模式 | 基础设施 |
| `shiyu-common/*` | 公共基础：Web、线程 Worker、Storage、MyBatis 封装 | 基础设施 |
| `shiyu-ai-web` | Controller、DTO 和 Web 适配层 | 基础设施 |

---

## 技术栈

| 领域 | 技术 |
|------|------|
| 语言 | Java 21（虚拟线程） |
| 框架 | Spring Boot 4.1 |
| AI 框架 | Spring AI 2.0 + LangChain4j |
| Agent 引擎 | LangGraph4j |
| Agent 流程图 | LangGraph4j + BaseNode |
| 认证授权 | Sa-Token |
| ORM | MyBatis-Flex |
| 数据库 | H2（开发）/ MySQL（生产） |
| 向量检索 | JVector（HNSW） |
| 嵌入模型 | BGE-small-zh（ONNX 本地部署） |
| MCP 协议 | Spring AI MCP |
| 缓存 | Caffeine |
| API 文档 | SpringDoc OpenAPI（UI 为可选 profile） |
| 可观测性 | OpenTelemetry + Micrometer + Prometheus |
| 日志 | Log4j2 |
| 调度 | XXL-Job |

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+

### 克隆 & 构建

```bash
git clone https://github.com/ShiRuYu/shiyu-ai.git
cd shiyu-ai
mvn clean install -DskipTests
```

### 配置 AI 平台

编辑 `shiyu-ai-bootstrap/src/main/resources/application.yml`，配置至少一个 LLM 平台：

```yaml
shiyu:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
    deepseek:
      base-url: https://api.deepseek.com
      api-key: sk-your-key
      model: deepseek-chat
```

### 启动

```bash
cd shiyu-ai
mvn -pl shiyu-ai-bootstrap -am -DskipTests package
java -jar shiyu-ai-bootstrap/target/shiyu-ai-bootstrap-1.0.0.jar --spring.profiles.active=dev
```

从 bootstrap 子目录直接执行 `spring-boot:run` 可能加载本机 Maven 仓库中的旧模块包；开发环境应先从根目录构建 reactor，确保运行的是当前源码。

### 发行包

```powershell
# Windows 云端基础包
./scripts/package-cloud-windows.ps1
# Windows 离线模型包（包含可选 BGE/ONNX 模型依赖）
./scripts/package-offline-windows.ps1
```

Linux 使用对应的 `scripts/package-cloud-linux.sh` 和
`scripts/package-offline-linux.sh`。生产包默认不包含观测、API 文档 UI 和 S3
适配器；需要时分别启用 `observability`、`api-docs-ui`、`s3` profile。

启动后访问：
- 应用端口：`http://localhost:9000`
- API 文档 JSON：`http://localhost:9000/v3/api-docs`
- API 文档 UI：仅在启用 `api-docs-ui` profile 时提供

---

## API 文档

| 分组 | 路径前缀 | 所属 |
|------|----------|------|
| Agent | `/api/agent/**` | 平台核心 |
| 模型 | `/api/model/**` | 平台基础设施 |
| 知识库 | `/api/knowledge/**` | 平台基础设施 |
| 记忆 | `/api/memory/**` | 平台基础设施 |
| 认证 | `/api/auth/**` | 平台基础 |
| 用量 | `/api/usage/**` | 平台基础设施 |
| 插件 | `/api/plugin/**` | 平台基础设施 |
| 教育 | `/api/education/**` | 业务扩展 |
| 记录 | `/api/record/**` | 业务扩展 |
| 系统 | `/api/system/**` | 基础设施 |

---

## 开发路线

```
第一阶段（已完成）  平台底座搭建
  ├── 多 LLM 平台接入     ✅
  ├── Agent 图编排引擎    ✅
  ├── 知识库与 RAG 引擎   ✅
  ├── 向量存储             ✅
  ├── 记忆系统             ✅
  ├── MCP 工具体系         ✅
  ├── 插件体系             ✅
  ├── 用量计量             ✅
  └── 多租户 RBAC 权限     ✅

第二阶段（当前）    业务方向扩展
  ├── Record 记录管理     ✅ 已上线
  ├── Education 智能教育  ✅ 已上线
  └── 更多业务方向...     🔜 待扩展

第三阶段（规划中）  平台能力增强
  ├── Agent 市场/模板
  ├── 可视化编排界面
  ├── 更丰富的 MCP 工具生态
  └── 性能与可观测性深化
```

---

## License

本项目采用 MIT 许可证。详见 [LICENSE](./LICENSE)。
