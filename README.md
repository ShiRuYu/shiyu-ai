# ShiYu AI · 拾羽 AI

> 基于 Java 21 + Spring Boot 4.x 的多模块 AI 服务平台 — 图编排 Agent、LiteFlow 聊天工作流、多平台 LLM 适配、MCP 工具、TTS 语音合成

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
- [License](#license)

---

## 项目简介

**ShiYu AI** 是一个面向 AI 应用场景的 Java 微服务集合，覆盖了 AI 聊天、Agent 自动化、MCP 工具集成和 TTS 语音合成等核心功能。项目采用模块化设计，每个模块可独立部署运行，通过 REST API 或 MCP 协议相互协作。

核心设计理念：

- 🧠 **双编排引擎** — `langgraph4j` 状态图驱动 Agent 行为，`LiteFlow` 规则引擎编排聊天流程
- 🔌 **多平台 LLM 适配** — 统一接口对接 OpenAI、Ollama、DeepSeek、硅基流动、OpenRouter
- 🧩 **MCP 协议集成** — 基于 Spring AI MCP 的工具服务体系
- 🔐 **企业级安全** — JWT + Sa-Token + Spring Security 三层认证体系
- ⚡ **响应式支持** — 同步/流式双模式，支持 SSE (Server-Sent Events)

---

## 架构总览

```
┌─────────────────────────────────────────────────────────┐
│                    Shiyu AI 系统架构                      │
├───────────┬───────────┬──────────┬──────────┬───────────┤
│ shiyu-auth│ shiyu-chat│shiyu-agent│shiyu-mcp│ shiyu-tts │
│  认证+权限  │  AI 聊天  │ Agent编排  │ MCP工具  │ 语音合成  │
│   :9002   │   :9001   │   :9000  │  :9003   │   :9004   │
├───────────┴───────────┴──────────┴──────────┴───────────┤
│                   shiyu-common (公共库)                  │
│  core │ web │ mybatis │ thread │ excel │ bom            │
└─────────────────────────────────────────────────────────┘
```

### Agent 图编排流程 (shiyu-agent)

```
用户输入 → 注册 Agent(Graph) → NodeFactory 编译 → 
  意图识别(INTENT) → 条件分支(CONDITION) → 
  LLM调用 / 工具调用 / RAG检索 / 记忆读写 / 输出格式化
```

### 聊天工作流 (shiyu-chat, LiteFlow)

```
chain "callFlow" = THEN(MEMORY_LOAD, INTENT, CHAIN_EXECUTE, MEMORY_SAVE)

CHAIN_EXECUTE 根据意图识别结果，路由到子链：
├─ chatDirect — 直接对话
├─ chatCoT    — Chain-of-Thought 思维链
└─ chatToT    — Tree-of-Thought 思维树
```

---

## 模块说明

### shiyu-agent (:9000) ⭐ 核心模块

基于 `langgraph4j` 的自定义 Agent 状态图引擎，支持 10 种可编排节点：

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
| `TRANSFORM` / `OUTPUT_FORMAT` | 数据转换与格式化 |

核心能力：
- **动态 Agent 注册** — 运行时注册/注销 Agent 定义
- **版本管理** — 支持多版本共存与热切换
- **同步/流式执行** — `POST /api/agent/{agentId}/execute` + SSE 流式端点
- **记忆系统** — 短期记忆（会话内） + 长期记忆（持久化）

### shiyu-chat (:9001)

多策略 AI 聊天引擎，基于 **LiteFlow 规则引擎** 编排：

- **3 种对话策略**：Direct（直接对话）、CoT（思维链推理）、ToT（思维树多方案）
- **5 大 AI 平台**：OpenAI、Ollama、DeepSeek、硅基流动 (SiliconFlow)、OpenRouter
- **多轮记忆**：短期记忆（对话历史）+ 长期记忆（持久化）
- **意图识别**：自动分类用户意图并路由到对应策略
- **流式响应**：支持 SSE 流式输出

### shiyu-auth (:9002)

企业级认证授权中心，提供完整的 RBAC 权限体系：

- **双重认证**：JWT Token + Spring Security 认证链
- **权限模型**：用户 / 角色 / 菜单 / 部门 / 岗位 / 租户
- **安全防护**：XSS 过滤、CSRF 防护、CORS 配置
- **密码策略**：委派式密码编码器（支持多种加密算法）

### shiyu-mcp (:9003)

基于 Spring AI MCP 协议的工具服务模块：

- **天气查询** — 调用 Open-Meteo 免费 API（无需 API Key）
- **空气质量** — 模拟数据（可对接真实 AQ 数据源）
- 通过 `@McpTool` 注解自动暴露为 MCP 工具

### shiyu-tts (:9004)

文本转语音服务，调用微软 Edge TTS WebSocket 接口：

- 支持多种语音（默认 `zh-CN-XiaoxiaoNeural`）
- 可调节语速（`rate` 参数）
- 章节分割 + 批量 TTS 处理（`SplitChapters`/`ChapterTTSProcessor`）

### shiyu-common

公共基础库，按子模块组织：

| 子模块 | 功能 |
|--------|------|
| `core` | 统一返回 `Result`、分页查询、12 种异常、工具类（JSON/Spring/反射/SQL/Servlet）、事务钩子、国际化 |
| `web` | XSS 过滤、请求流重复读取、OpenAPI 文档配置、资源拦截器 |
| `mybatis` | MyBatis-Flex 封装 `BaseMapperFlex`、`BaseEntity`/`TenantEntity`、P6Spy SQL 日志 |
| `thread` | 线程池管理、虚拟线程工厂、平台线程工厂、OpenTelemetry 集成、Micrometer 指标 |
| `excel` | EasyExcel 封装、字典转换、枚举转换、大数字处理、单元格合并策略 |
| `bom` | Maven BOM 统一版本声明 |

---

## 技术栈

| 领域 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 4.0.1 |
| AI 编排 | langgraph4j | 1.8.10 |
| AI 工具链 | langchain4j / Spring AI | 1.12.2 / 2.0.0-M1 |
| 流程编排 | LiteFlow | 2.15.3 |
| 认证授权 | Sa-Token / Spring Security | 1.45.0 |
| ORM | MyBatis-Flex / MyBatis-Plus | 1.11.5 / 3.5.16 |
| 数据库 | MySQL / H2 | 9.4.0 |
| 连接池 | Druid | 1.2.27 |
| 对象映射 | MapStruct + Lombok | 1.5.0 / 1.18.42 |
| 工具库 | Hutool / Guava / Caffeine | 5.8.43 / 33.5.0 / 3.2.3 |
| API 文档 | SpringDoc OpenAPI + Knife4j | 3.0.2 / 4.5.0 |
| 响应式 | Reactor (Flux) | — |
| 日志 | Log4j2 | — |
| 调度 | XXL-Job | 3.3.2 |
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

编辑相应模块的配置文件（示例路径：`shiyu-chat/src/main/resources/config/config.yml`）：

```yaml
shiyu:
  ai:
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

每个模块独立启动，端口配置见 `application.yml`：

```bash
# Agent 服务 (9000)
cd shiyu-agent
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 聊天服务 (9001)
cd shiyu-chat
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 认证服务 (9002)
cd shiyu-auth
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# MCP 工具服务 (9003)
cd shiyu-mcp
mvn spring-boot:run

# TTS 语音服务 (9004)
cd shiyu-tts
mvn spring-boot:run
```

### Maven Profile

| Profile | 用途 | 默认 |
|---------|------|------|
| `local` | 本地开发（debug 日志） | |
| `dev` | 开发环境（默认） | ✅ |
| `prod` | 生产环境（warn 日志） | |

---

## 配置指南

### 聊天服务 (shiyu-chat)

```yaml
shiyu:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
    openai:
      base-url: https://api.openai.com/v1
      api-key: sk-xxx
      model: gpt-4o
    openrouter:
      base-url: https://openrouter.ai/api
      api-key: sk-xxx
      model: x-ai/grok-4.1-fast
    siliconflow:
      base-url: https://api.siliconflow.cn
      api-key: sk-xxx
      model: THUDM/GLM-Z1-9B-0414
    deepseek:
      base-url: https://api.deepseek.com
      api-key: sk-xxx
      model: x-ai/grok-4.1-fast
  memory:
    enabled: true
    max-short-term-memories: 10
    max-long-term-memories: 50
    max-history-records: 5
  intent:
    platform: SILICON_FLOW
    model: THUDM/GLM-Z1-9B-0414
```

### TTS 服务 (shiyu-tts)

```yaml
tts:
  websocket-url: wss://speech.platform.bing.com/…
```

### API 文档访问

启动各模块后访问：

```
http://localhost:{port}/swagger-ui/index.html
http://localhost:{port}/v3/api-docs
```

---

## 开发规范

- **Lombok** — `@Data`、`@Slf4j`、`@RequiredArgsConstructor` 简化代码
- **MapStruct** — BO ↔ VO 对象映射
- **Jakarta Validation** — `@Valid` 参数校验分组（AddGroup / EditGroup / QueryGroup）
- **XSS 过滤** — 全局 XSS 过滤器防止跨站脚本攻击
- **统一异常** — `@ControllerAdvice` + 12 种业务异常 + 统一 `Result<T>` 返回
- **分页查询** — `PageQuery` + `PageData<T>` 统一分页模型
- **国际化** — `i18n/messages` 资源文件
- **OpenTelemetry** — 线程池上下文透传 + 调用链追踪

### 项目分层

```
controller/  ←  REST 接口层
service/     ←  业务逻辑层
  impl/      ←  实现类
repository/  ←  仓储层（MyBatis-Flex 操作）
mapper/      ←  MyBatis 映射接口
domain/      ←  领域模型
  bo/        ←  业务对象
  vo/        ←  视图对象
  request/   ←  请求参数
dal/
  dataobject/ ← 数据对象（DO）
  mapper/    ← 数据访问映射
config/      ←  配置类
```

---

## License

本项目采用 MIT 许可证。详情请查看 [LICENSE](./LICENSE) 文件。
