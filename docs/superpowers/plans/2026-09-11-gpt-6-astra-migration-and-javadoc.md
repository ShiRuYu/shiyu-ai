# GPT-6 Astra 迁移与 JavaDoc 扫描实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 OpenAI provider 迁移到 GPT-6 Astra 的 Responses API，同时保持 ShiYu 的内部聊天/Agent/SSE 契约稳定，并为生产源码的公共 Java 接口补充准确 JavaDoc。

**Architecture:** 新增 OpenAI Responses 专用 transport，与现有 OpenAI-compatible、DeepSeek、Ollama、OpenRouter 和 SiliconFlow 路径并存。内部仍以 `ChatRequest`、`ChatResponse` 和 provider-neutral stream event 为边界，外部 facade 不改；JavaDoc 扫描作为独立的文档性任务完成。

**Tech Stack:** Java 21、Spring Boot 4.1、Maven reactor、LangChain4j 1.16.3、`java.net.http.HttpClient`、Reactor Flux、H2 baseline、Vue/Vben、Vitest、Playwright。

**Spec:** `docs/superpowers/specs/2026-09-11-gpt-6-astra-migration-design.md`

## Global Constraints

- OpenAI model id 固定为 `gpt-6-astra`；reasoning effort 初始为 `low`。
- OpenAI tool calling 使用 Responses API；不得向 Astra 发送 `temperature`、`top_p`、`top_logprobs` 或 `logprobs`。
- DeepSeek、Ollama、OpenRouter、SiliconFlow 的现有 transport 和默认模型保持行为不变。
- API key 只通过环境变量或现有密钥管理路径注入，不写入源码、测试 fixture 或日志。
- 保持 `ChatEngine`、`ChatRequest`、`ChatResponse`、SSE event type 和现有外部接口兼容。
- JavaDoc 改动不得改变业务逻辑；不修改 `_validation`、备份目录、生成文件或测试输出。

### Task 1: 建立迁移基线与评测样本

**Files:**
- Read: `modules/applications/shiyu-ai-bootstrap/src/main/resources/application.yml`
- Read: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/adapter/impl/GenericPlatformAdapter.java`
- Read: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/chat/impl/ChatEngineImpl.java`
- Create: `docs/superpowers/evals/gpt-6-astra-baseline.md`

- [ ] 记录现有 OpenAI、DeepSeek、Ollama、路由 fallback、同步、流式、工具调用和 usage 路径。
- [ ] 从现有测试抽取至少 20 个代表性用例，覆盖普通问答、系统消息、多轮消息、工具调用、流式增量、推理增量、结构化输出、图像输入、取消和 provider 错误。
- [ ] 为每个用例记录请求形状、预期内部事件和可比较指标；不得保存真实 API key 或个人数据。

### Task 2: 固化 OpenAI Responses 配置边界

**Files:**
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/config/PlatformProperties.java`
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/adapter/config/PlatformConfig.java`
- Modify: `modules/applications/shiyu-ai-bootstrap/src/main/resources/application.yml`
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/domain/model/PlatformAdapterType.java`
- Test: `modules/domains/model/shiyu-model-implementation/src/test/java/com/shiyu/ai/model/implementation/infrastructure/adapter/ModelManagerTest.java`

- [ ] 增加 `transport/adapterType` 与 `reasoningEffort` 的明确字段或等价配置，并限定其只作用于 OpenAI Responses。
- [ ] 将环境变量默认值改为 `AI_OPENAI_MODEL:gpt-6-astra`，默认 reasoning effort 设为 `low`。
- [ ] 为非法 reasoning effort、空 model、OpenAI-compatible 误用 Responses transport 增加验证测试。
- [ ] 运行 `mvn -pl modules/domains/model/shiyu-model-implementation -am -Dtest=ModelManagerTest test`，确认配置边界测试通过。

### Task 3: 实现 OpenAI Responses 同步与流式 transport

**Files:**
- Create: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/adapter/impl/OpenAiResponsesAdapter.java`
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/adapter/ModelManager.java`
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/chat/impl/ChatEngineImpl.java`
- Test: `modules/domains/model/shiyu-model-implementation/src/test/java/com/shiyu/ai/model/implementation/infrastructure/adapter/impl/OpenAiResponsesAdapterTest.java`
- Test: `modules/domains/model/shiyu-model-implementation/src/test/java/com/shiyu/ai/model/implementation/infrastructure/chat/ChatEngineImplTest.java`

- [ ] 先以当前 LangChain4j 依赖做编译探针；若未提供完整 Responses streaming/tool-call API，使用现有 `HttpClient` 模式实现 `POST /v1/responses`，不引入未经确认的 SDK。
- [ ] 将内部消息转换为 Responses `input`，将函数 schema 转为 `tools`，将 reasoning effort 写入 `reasoning.effort`，将输出上限写入 `max_output_tokens`。
- [ ] 明确过滤 Astra 不支持的采样与 logprob 字段；非 OpenAI 适配器继续保留原有温度配置。
- [ ] 映射非流式 output text、function call、reasoning、response id、finish/incomplete 状态和 usage 到现有 `ChatResponse`。
- [ ] 映射流式 `response.output_text.delta`、function-call arguments delta、reasoning delta、usage、completed、failed、cancelled 到现有事件类型，并支持 HTTP 取消。
- [ ] 测试 JSON 序列化、错误状态、重试上限、空 output、多个 tool call、部分 tool arguments 和 usage 缺失场景。

### Task 4: 接入路由、模型目录和数据库基线

**Files:**
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/java/com/shiyu/ai/model/implementation/infrastructure/gateway/ModelRouter.java`
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/resources/db/baseline/h2/seed/model/03_model.sql`
- Modify: `modules/domains/model/shiyu-model-implementation/src/main/resources/db/baseline/h2/schema/model/04_model.sql` only if a schema field is proven necessary
- Modify: model management DTO/controller and `shiyu-ui/apps/web-naive/src/features/model/pages/platform/modules/form.vue` only for new explicit transport/reasoning fields
- Test: model catalog, route and database initializer tests

- [ ] 注册 `gpt-6-astra` 的 chat、stream、tool_calls、parallel_tool_calls、structured、multimodal、reasoning 和 json_schema 能力。
- [ ] 让新安装的 H2 基线将 Astra 作为 OpenAI 默认模型，同时保留旧模型记录以支持回滚。
- [ ] 验证运行时热加载、按租户路由、模型列表和 UI 表单不会把 Responses 专用参数发送给其他 provider。
- [ ] 运行模型目录、路由和数据库初始化的针对性 Maven 测试。

### Task 5: 保持会话与 OpenAI-compatible facade 兼容

**Files:**
- Modify: `modules/domains/conversation/shiyu-conversation-implementation/src/main/java/com/shiyu/ai/conversation/implementation/web/OpenAiCompatibleController.java` only where Responses-specific fields need pass-through
- Test: `modules/domains/conversation/shiyu-conversation-implementation/src/test/java/com/shiyu/ai/conversation/implementation/web/OpenAiCompatibleControllerTest.java`
- Test: conversation generation and SSE tests

- [ ] 保持 `/api/conversation/chat/completions` 与 `/api/conversation/responses` 的请求/响应兼容形状。
- [ ] 确认 facade 的 response id、usage、tool call、reasoning 和 `[DONE]` 行为在 Astra transport 下仍稳定。
- [ ] 验证 store=true、conversation id、runtime usage、取消和失败状态不会重复持久化或丢失租户归属。

### Task 6: Prompt、观测和灰度回滚

**Files:**
- Modify: agent prompt/config files identified in Task 1
- Modify: usage/pricing metadata only after official pricing verification
- Create: `docs/superpowers/evals/gpt-6-astra-results.md`

- [ ] 将静态指令、工具描述和动态用户数据分层，明确完成条件、工具边界、输出格式和失败策略。
- [ ] 对 20 个基线用例比较成功率、工具选择、结构化输出有效率、token 使用、端到端延迟和错误率。
- [ ] 通过显式 route policy 或平台配置灰度启用 Astra；定义旧模型回退条件并记录实际恢复步骤。
- [ ] 对需要 EU data residency 的部署确认使用 Standard processing，不配置不兼容的 fast/priority mode。

### Task 7: 全量验证与迁移交付

**Files:**
- Read: all changed backend and frontend files
- Read: `README.md`, `README.en.md`, `docs/技术文档.md`, `docs/部署运维手册.md`

- [ ] 运行 `mvn -pl modules/applications/shiyu-ai-bootstrap -am test`。
- [ ] 运行 `mvn -pl modules/applications/shiyu-ai-bootstrap -am -DskipTests package`。
- [ ] 在 `shiyu-ui` 运行 `pnpm test:contract`、相关 Vitest 测试和生产构建。
- [ ] 运行 `git diff --check`，检查日志不包含 key、token 或完整用户 prompt。
- [ ] 更新部署文档中的环境变量、模型目录、灰度和回滚步骤。

### Task 8: 扫描并补充 JavaDoc（本次执行）

**Files:**
- Modify: production `src/main/java` interfaces and ports under `modules/`, `shared/`, and `tests` only when they define public contracts
- Modify: public DTO/record and cross-module service methods that lack meaningful JavaDoc
- Exclude: private methods, generated sources, backup directories, `_validation`, test doubles and unchanged implementation details

- [ ] 扫描所有接口声明、公共 record/class、公共方法和控制器 endpoint，按模块归类并去重。
- [ ] 为每个跨模块接口补充职责、参数、返回值、异常或事件语义；不复制方法名作为空注释。
- [ ] 为模型、会话、Agent、知识库、治理和工具模块中最关键的公共边界补充 JavaDoc，保持现有中文/英文风格。
- [ ] 运行 `git diff --check`、后端编译和受影响模块测试，确认只产生文档性改动。
