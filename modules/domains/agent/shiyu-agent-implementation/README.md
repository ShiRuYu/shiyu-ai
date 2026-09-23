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

## 边界

其他领域面向 Agent 的集成应依赖 `shiyu-agent-contract`；实现模块负责编排和执行，不应把数据库实体当作跨领域 API。

## 主要包

`com.shiyu.ai.agent`、`com.shiyu.ai.agent.implementation`、`com.shiyu.ai.agent.implementation.builder`、`com.shiyu.ai.agent.implementation.cache`、`com.shiyu.ai.agent.implementation.checkpoint`、`com.shiyu.ai.agent.implementation.config`、`com.shiyu.ai.agent.implementation.domain`、`com.shiyu.ai.agent.implementation.evaluation` 等

## 内部模块依赖

`shiyu-agent-contract`、`shiyu-shared-kernel`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-model-contract`、`shiyu-knowledge-contract`、`shiyu-memory-contract`、`shiyu-tooling-contract`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/agent/shiyu-agent-implementation -am test -Ddependency-check.skip=true`
