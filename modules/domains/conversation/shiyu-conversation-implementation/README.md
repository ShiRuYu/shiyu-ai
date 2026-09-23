# shiyu-conversation-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-conversation-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现聊天产品、对话及消息管理和生成请求编排，并提供普通 Web 与流式生成入口。

## 功能说明

- 产品与会话：维护聊天产品、会话和消息；支持会话/消息关系组织及相关查询、更新和删除。
- 生成流程：组装 Prompt 和安全约束，申请生成准入，调用 Agent/模型相关 contract，并将生成状态与用量交给治理协作接口。
- 流式响应：向客户端发送生成事件与状态；运行记录支持处理中断后的恢复/续接场景，具体传输由 Web 层适配。
- 兼容与导入：提供 OpenAI-compatible 请求入口，并使用独立预览存储处理角色/会话导入的预览阶段，避免预览数据直接落入正式会话。
- 持久化与 API：conversation Controller 对外提供接口，关系数据由 MyBatis 持久化；H2 schema/seed 支持开发环境。
- Agent 与模型协作通过 contract，聊天域不持有供应商 SDK 的跨模块公共接口。

## 一次生成请求如何流转

1. `ChatProductController`、`ConversationController`、`MessageController` 分别管理聊天产品、会话和消息；`JdbcChatProductRepository`、`JdbcConversationRepository` 保存对应关系数据。
2. `GenerationController` 建立生成运行，`PromptAssemblyService` 与 `ConversationPromptService` 组装上下文，`GenerationRunner` 调用已装配的 Agent/模型能力执行生成。
3. 准入、用量记录通过 conversation contract 的 `GenerationAdmission`、`GenerationUsageSink` 与平台组合层连接；`JdbcGenerationRepository` 保存运行和事件，便于查询生成状态。
4. `OpenAiCompatibleController` 提供兼容协议入口；`CharacterImportPreviewStore`、`ConversationImportPreviewStore` 保存导入预览，确认前不应把预览内容当成正式会话数据。

HTTP 流式传输、生成生命周期、会话持久化是三层不同职责。请求的用户和租户范围由服务端上下文决定，生成失败时仍需正确结束运行并释放已申请的准入资源。

## 使用前提与示例

- **前提**：应用已装配数据库、模型/Agent 实现；若要求配额生效，还需组合层提供真正的 `GenerationAdmission` 和用量接收器。请求主体必须能访问目标聊天产品和会话。
- **使用**：先在 `/api/conversation/chat-products` 选择或配置产品，使用 `/api/conversation/conversations` 创建会话，再通过 `/api/conversation/messages` 管理消息；调用 `/api/conversation/generations` 开始生成并消费其事件/状态。
- **注意**：导入预览与正式会话是两个阶段，确认前不能把预览数据当已持久化消息；流式响应失败时仍应结束运行并释放配额预留。

## 边界

其他领域如需生成生命周期或用量协作，应依赖 `shiyu-conversation-contract`；会话存储模型和 Controller DTO 不作为跨领域契约。

## 主要包

`com.shiyu.ai.conversation.implementation`、`com.shiyu.ai.conversation.implementation.application`、`com.shiyu.ai.conversation.implementation.domain`、`com.shiyu.ai.conversation.implementation.infrastructure`、`com.shiyu.ai.conversation.implementation.web`

## 内部模块依赖

`shiyu-conversation-contract`、`shiyu-model-contract`、`shiyu-agent-contract`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/conversation/shiyu-conversation-implementation -am test '-Ddependency-check.skip=true'`
