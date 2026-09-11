# GPT-6 Astra 迁移设计

## 目标

将 ShiYu AI 的 OpenAI 路径迁移到 `gpt-6-astra`，保持现有 `ChatEngine`、会话、Agent、SSE 和 OpenAI-compatible 外部接口稳定；DeepSeek、Ollama、OpenRouter、SiliconFlow 等非 OpenAI provider 不随本次迁移改变。

迁移完成的可验证结果是：OpenAI provider 使用 Responses API；同步、流式、工具调用、推理内容、usage、取消和错误映射都能通过现有内部契约；所有现有后端与前端契约检查通过；Java 公共接口和跨模块边界具备准确、简洁的 JavaDoc。

## 事实基线

- 默认模型在 `modules/applications/shiyu-ai-bootstrap/src/main/resources/application.yml` 和 `PlatformProperties` 中仍为 `gpt-4o-mini`。
- `ModelManager.loadHardcodedDefaults()` 的 OpenAI 回退模型仍为 `gpt-4o`。
- `GenericPlatformAdapter` 通过 LangChain4j 的 `OpenAiChatModel` 与 `OpenAiStreamingChatModel` 创建 Chat Completions 风格客户端，并统一发送 `temperature`、`maxTokens`、`returnThinking` 和 `parallelToolCalls`。
- `ChatEngineImpl` 已有 provider-neutral 的同步/流式响应、工具调用、推理增量和 usage 事件模型。
- `OpenAiCompatibleController` 的 `/api/conversation/responses` 是 ShiYu 对外的兼容 facade；它当前仍委托 `ChatEngine`，不能作为底层已经切换到 OpenAI Responses API 的证据。
- 模型平台和模型基线位于 `MODEL_AI_PLATFORM`、`MODEL_AI_MODEL` 及 H2 seed 脚本，且支持运行时热加载。

## 官方约束

OpenAI 文档规定 GPT-6 Astra 的 model id 为 `gpt-6-astra`，可用 reasoning effort 为 `low`、`medium`、`high`、`xhigh`、`max`；工具调用应使用 Responses API；迁移时应移除不支持的 `temperature`、`top_p`、`top_logprobs` 和 `logprobs` 参数。参考：[GPT-6 Astra 模型](https://developers.openai.com/api/docs/models/gpt-6-astra) 与 [Model guidance](https://developers.openai.com/api/docs/guides/latest-model)。

## 推荐架构

采用“OpenAI Responses 专用适配器 + 现有 provider 兼容适配器并存”的方案：

1. 增加明确的 OpenAI Responses transport/adapter；不要把所有 provider 改写为 Responses。
2. `ModelManager` 根据 provider/adapter type 将 `OPENAI` 路由到新适配器，其他 provider 继续使用现有 `GenericPlatformAdapter` 或专用实现。
3. 新适配器把内部 `ChatRequest` 转成 Responses `input`、`tools`、`reasoning`、`max_output_tokens`，再把 response item、function call、reasoning delta、text delta 和 usage 转回现有 `ChatResponse`/`ModelStreamEvent`。
4. `ChatEngineImpl`、`OpenAiCompatibleController`、前端 SSE 协议和持久化模型保持不变；只在必要处增加响应字段映射和取消能力。
5. 优先复用当前 `java.net.http.HttpClient` 方式，避免在确认 LangChain4j 1.16.3 支持 Responses API 前引入新的 SDK；若编译探针证明现有依赖已提供完整 Responses streaming/tool-call 支持，则可采用官方 SDK，但必须保留等价的内部映射测试。

## 配置与数据迁移

- `AI_OPENAI_MODEL` 默认值改为 `gpt-6-astra`。
- 增加 OpenAI 的 `reasoning-effort` 配置，默认 `low`；该配置只进入 Astra 请求，不影响其他 provider 的温度参数。
- 将 `maxTokens` 语义映射为 Responses 的 `max_output_tokens`，同时保留旧字段以兼容数据库和管理页面。
- 为平台配置增加可识别的 Responses adapter type，或在 `extraConfig` 中显式声明 `transport=responses`；不得仅凭模型字符串隐式改变所有 OpenAI-compatible 平台。
- H2 seed 新增/替换 OpenAI 默认模型 `gpt-6-astra`，保留旧模型记录以支持回滚；历史 fixture 与其他 provider 模型不改。
- `ModelRouter` 注册 Astra 的 `chat`、`stream`、`tool_calls`、`parallel_tool_calls`、`structured`、`multimodal`、`reasoning` 和 `json_schema` 能力，并声明有效 reasoning levels 与输出上限。
- 更新模型价格/usage 元数据时以当前官方定价为准，不在代码中复制未经验证的价格常量。

## Prompt 与行为迁移

- 审计 Agent 节点默认提示词、系统提示词、工具描述和结构化输出要求，删除依赖温度调节的隐含行为。
- 对需要持续执行的流程明确写出完成条件、失败处理、工具使用边界和输出格式。
- 维持动态 prompt 的静态前缀在前、用户数据在后的布局，便于缓存；不把当前日期硬编码进提示词。
- 通过代表性用例比较旧模型与 Astra 的答案质量、工具选择、token 使用和端到端延迟，不以单次结果判断迁移成功。

## JavaDoc 扫描设计

JavaDoc 只覆盖生产源码中的公共边界：接口、端口、公共 DTO/record、跨模块公共方法、控制器公开 endpoint 和关键领域服务。私有方法、生成文件、备份目录、测试 double 和纯实现细节不批量添加空泛注释。注释必须说明职责、关键参数、返回值、异常或事件语义，且不改变运行行为。

## 验证与回滚

- 单元层验证 request/response JSON、参数过滤、tool call、多轮消息、reasoning、stream event、usage 和取消。
- 集成层使用真实 OpenAI key 在受控环境验证同步、SSE、工具调用、结构化输出、错误重试和模型路由；不把凭据写入仓库。
- 回归层运行 Maven reactor 测试、OpenAPI/前端契约测试，并检查 `git diff --check`。
- 先通过显式平台配置或 route policy 灰度启用 Astra；失败时将默认模型切回旧记录或关闭 Responses adapter，不回滚其他 provider 的改动。
