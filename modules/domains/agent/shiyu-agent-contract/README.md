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

## 边界

教育、会话、知识、记忆和工具等协作方可依赖这些契约；本模块不依赖 Agent implementation。

## 主要包

`com.shiyu.ai.agent.contract`、`com.shiyu.ai.agent.contract.node`、`com.shiyu.ai.agent.contract.runtime`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/agent/shiyu-agent-contract -am test -Ddependency-check.skip=true`
