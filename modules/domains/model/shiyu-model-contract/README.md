# shiyu-model-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-model-contract`
- **分类**：领域模块 · Contract

## 作用

定义跨领域使用模型推理、路由、目录和嵌入能力时共享的数据结构与端口。

## 契约内容

- `ChatEngine` 与结构化聊天消息/请求/响应模型定义聊天调用契约。
- `ModelRoutingPort` 和 `ModelCatalogPort` 提供模型选择与目录查询入口，隐藏 provider 选择和配置存储细节。
- `EmbeddingService` 提供文本嵌入协作接口，供知识等模块构建索引或执行语义检索。
- 契约不包含模型厂商 SDK、密钥、HTTP 适配、配置持久化和具体路由策略实现。

## 推理调用协议

- `ChatEngine.chat` 返回一次完整的 `ChatResponse`，`ChatEngine.stream` 返回 `Flux<ChatResponse>`；`ChatRequest` 包含消息和可选工具定义，模型响应可以携带工具调用内容。
- `ModelRoutingPort` 查询可用模型、解析模型所属平台并提供默认平台；`ModelCatalogPort` 提供已启用平台及模型的数量，用于跨模块目录能力。
- `EmbeddingService.embed`、`embedBatch` 与 `dimension` 供知识索引和检索使用；嵌入请求应携带可信租户，而不能从供应商配置推断租户。

本 contract 不依赖具体模型 provider，但流式接口使用 Reactor `Flux`，因此它不是“零第三方依赖”的纯 Java 包；引入该模块的消费者需接受该响应式类型。

## 使用前提与示例

- **前提**：调用方引入 contract；应用运行时需装配模型实现，配置可用平台、模型与凭证。流式调用方还需处理 Reactor `Flux` 的取消和错误信号。
- **使用**：注入 `ChatEngine` 后以 `chat(request)` 获取完整 `ChatResponse`，或以 `stream(request)` 处理逐段响应；知识索引注入 `EmbeddingService`，使用 `embed(tenantId, text)` 得到向量，并通过 `dimension()` 校验索引维度。
- **路由**：需要列出可用模型或解析默认平台时使用 `ModelRoutingPort`/`ModelCatalogPort`，不要在 Agent、知识模块直接读取 provider 配置或密钥。

## 边界

Agent、知识和会话等协作模块依赖这些类型；契约不依赖 model implementation。

## 主要包

`com.shiyu.ai.model.contract`、`com.shiyu.ai.model.contract.api`、`com.shiyu.ai.model.contract.model`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/model/shiyu-model-contract -am test '-Ddependency-check.skip=true'`
