# shiyu-model-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-model-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现模型配置、provider 适配与路由，以及聊天、嵌入和多媒体 AI 能力。

## 功能说明

- 模型管理：维护平台连接和模型目录，提供模型增删改查、能力配置、健康检查与可用性信息。
- Provider 与路由：适配 DeepSeek HTTP 接口和 Ollama 等模型平台，根据配置执行模型选择、路由及能力匹配。
- 推理能力：实现结构化聊天及流式事件、文本嵌入，并支持语音合成、翻译、图像理解和图像生成等多媒体任务。
- 安全配置：通过 `shiyu.ai` 配置模型 provider 与密钥来源；密钥不作为普通模型响应数据返回。
- Web 与持久化：模型 Controller 暴露平台、模型与推理相关 API，MyBatis 存储平台和模型配置；H2 schema 提供开发基线。
- 其他领域通过 `shiyu-model-contract` 使用聊天、目录、路由和嵌入能力，不依赖 provider adapter 或配置实体。

## 模型调用路径

1. `AiPlatformController`、`AiModelController` 管理平台连接和模型条目；`AiPlatformServiceImpl`、`AiModelServiceImpl` 使用领域 Repository 持久化配置。
2. `ModelRouter` 根据 `ModelRoutePolicy` 与 `ModelProviderCapabilities` 选择可用平台和模型；路由不能把客户端模型名直接当成已授权的供应商调用。
3. `DeepSeekHttpProvider`、`OllamaPlatformAdapter` 和 `GenericPlatformAdapter` 封装供应商协议；`ModelGatewayController` 对外接收模型调用请求，底层 adapter 决定实际 HTTP/本地执行方式。
4. `LangChain4jEmbeddingService` 实现 contract 的嵌入服务；`MediaController` 负责多媒体能力入口。知识索引和 Agent 仅面向 contract，而非这些 provider 类。

模型配置、路由决策与实际调用分别有独立职责；需要新增供应商时，应实现适配器和能力声明，并验证密钥保护、流式响应及调用用量，而不是在 Controller 中加入供应商分支。

## 使用前提与示例

- **前提**：模型平台与具体模型条目已配置并启用；远端 provider 需要可用的服务地址和凭证，本地 Ollama 需要相应服务/模型。知识索引使用嵌入前还要确认向量维度匹配。
- **使用**：管理端通过 `GET /api/model/platforms` 查看平台、`POST /api/model/platforms` 建立平台（分别要求 `agent:platform:list`、`agent:platform:create`），再在 `/api/model/model-configurations` 配置模型。业务代码经 `ChatEngine`、`EmbeddingService`、`ModelRoutingPort` 调用，不直接依赖 provider adapter。
- **注意**：平台已配置不代表一定可调用；路由会评估启用状态与能力。密钥留在服务端，客户端模型名不能绕过路由与授权。

## 边界

provider 密钥、供应商协议和路由策略属于本模块实现；其他领域只依赖模型 contract，并按各自授权规则控制调用入口。

## 主要包

`com.shiyu.ai.model.implementation`、`com.shiyu.ai.model.implementation.application`、`com.shiyu.ai.model.implementation.domain`、`com.shiyu.ai.model.implementation.infrastructure`、`com.shiyu.ai.model.implementation.persistence`、`com.shiyu.ai.model.implementation.web`

## 内部模块依赖

`shiyu-model-contract`、`shiyu-shared-kernel`、`shiyu-common-mybatis`、`shiyu-common-web`、`shiyu-common-foundation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/model/shiyu-model-implementation -am test '-Ddependency-check.skip=true'`
