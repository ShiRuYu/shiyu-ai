# shiyu-agent-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-agent-contract`
- **分类**：领域模块 · Contract

## 作用

定义 Agent 图节点、运行时和上下文协作接口，使其他领域可以接入 Agent 能力而不依赖其实现或存储技术。

## 契约内容

- 节点扩展模型：`BaseNode` 以及节点输入、输出、配置、创建器和类型契约，用于声明图节点的配置与执行形态。
- 运行接口：`AiRuntimePort`、AiRun 状态/事件/Repository 契约，定义图执行和运行生命周期的协作边界。
- 上下文协作：`ContextAssemblyPort`、`ContextRetrievalPort` 及其请求/结果模型，供知识、记忆等领域向运行上下文提供能力。
- 执行历史契约：为调用方提供查询 Agent 执行历史所需的服务边界。
- 本模块不创建图、执行节点、连接数据库或定义 HTTP 接口；具体实现由 `shiyu-agent-implementation` 提供，契约保持框架无关。

## 扩展与运行协议

- 自定义节点以 `NodeType`、`NodeConfig`、`NodeInput` 和 `NodeOutput` 表达输入输出，`NodeCreator` 根据配置创建 `BaseNode`；`NodeFields` 约束可用字段，避免图配置凭任意字符串猜测节点协议。
- `AiRuntimePort` 管理运行创建、完成、事件追加及按租户/所有者查询；`AiRunRepository` 是持久化端口，具体 JDBC 或内存实现不属于 contract。
- `ContextRetrievalPort` 提供单一来源的候选内容；`ContextAssemblyPort` 合并上下文并返回 `ContextTrace`。`ContextPolicy` 决定候选内容是否可读，但最终实现仍要维护租户和资源归属。
- `ExecutionHistoryService` 为外部调用方记录执行开始与完成，图构建、节点执行和检查点留在 implementation。

该模块不依赖 Spring 或数据库，但源码使用 SLF4J API 和 provided Lombok；“框架无关”在这里特指不把应用容器、ORM 与 Web 类型带入跨领域协议。

## 边界

教育、会话、知识、记忆和工具等协作方可依赖这些契约；本模块不依赖 Agent implementation。

## 主要包

`com.shiyu.ai.agent.contract`、`com.shiyu.ai.agent.contract.node`、`com.shiyu.ai.agent.contract.runtime`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/agent/shiyu-agent-contract -am test -Ddependency-check.skip=true`
