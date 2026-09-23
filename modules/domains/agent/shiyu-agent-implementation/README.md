# shiyu-agent-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-agent-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现 Agent 定义、流程图编排、运行时执行和评估能力，并通过 Agent HTTP API 向产品界面及其他调用方提供服务。

## 功能说明

- 定义管理：创建和编辑 Agent 应用、图结构、Prompt 与意图配置；校验节点和连接关系，维护版本并支持发布。
- 图执行：运行条件、意图识别、LLM、知识检索、记忆、工具调用、Agent 调用、数据变换和输出等节点。
- 运行治理：维护 AiRun 生命周期、运行事件、执行轨迹和检查点；对需要审批的工具调用提供待审和处理流程。
- 评估与审计：组织评估案例/数据集、指标和结果，并记录运行时间线与相关审计事件。
- Web/API 与存储：`/api/agent/**` 下的 Controller 提供定义、版本、运行与执行相关接口；持久化适配器与内存实现分别服务于对应运行和测试场景，H2 资源提供开发基线。
- 通过 Agent contract 接收模型、知识、记忆与工具服务，不在本模块复制这些领域的实现。

## 图构建与运行链路

1. `AgentDefinitionController`、`AgentVersionController` 和 `AgentVersionCrudController` 管理定义、草稿与版本；`StateGraphBuilder` 验证图节点/边后生成可执行结构。
2. `NodeFactory`、`NodeInstanceFactory` 按节点类型创建执行实例。知识检索、记忆检索、LLM 与工具节点通过各领域 contract 接入能力，不直接访问外域 Repository。
3. `AgentExecutor` 与 `AgentRuntimeImpl` 推进执行；`AiRuntimeService` 管理运行记录，`AgentExecutionLifecycle` 和 `AgentRuntimeEventBridge` 将状态及事件写入运行仓储。
4. `ContextAssemblyService` 根据检索端口与 `DefaultContextPolicy` 组织上下文；`ToolExecutionPipeline` 配合 `ToolApprovalService` 处理需要批准的工具执行。
5. `CheckpointManager` 管理执行恢复点，`EvaluationService` 和评估仓储保存数据集/运行结果；`ExecutionController`、`EvaluationController` 等 Web 入口分别暴露执行与评估功能。

内存 Repository 用于明确选择的运行/测试场景，JDBC 仓储承载持久化场景；两者不是可随意互换的运行保障。Agent 的运行状态、外部模型调用、知识检索和工具权限要分别由各自边界负责。

## 边界

其他领域面向 Agent 的集成应依赖 `shiyu-agent-contract`；实现模块负责编排和执行，不应把数据库实体当作跨领域 API。

## 主要包

`com.shiyu.ai.agent`、`com.shiyu.ai.agent.implementation`、`com.shiyu.ai.agent.implementation.builder`、`com.shiyu.ai.agent.implementation.cache`、`com.shiyu.ai.agent.implementation.checkpoint`、`com.shiyu.ai.agent.implementation.config`、`com.shiyu.ai.agent.implementation.domain`、`com.shiyu.ai.agent.implementation.evaluation` 等

## 内部模块依赖

`shiyu-agent-contract`、`shiyu-shared-kernel`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-model-contract`、`shiyu-knowledge-contract`、`shiyu-memory-contract`、`shiyu-tooling-contract`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/agent/shiyu-agent-implementation -am test -Ddependency-check.skip=true`
