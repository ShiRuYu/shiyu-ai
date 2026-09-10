# shiyu-agent-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-agent-implementation`
- **分类**：领域模块 · Implementation
- **源码规模**：生产 Java 207 个，测试 Java 55 个

## 作用

Agent 智能体实现模块，承载该领域的应用服务、领域模型和基础设施适配。

## 职责

- 负责智能体定义、图编排、节点执行与运行时生命周期。
- 在领域内部组织 application、domain、persistence、infrastructure 和 web 等实现职责。
- 通过 contract、shared-kernel 与公共基础设施协作，对外暴露领域服务。

## 边界

实现细节只在组合根和需要该能力的应用层装配；其他领域应依赖 contract。

## 主要包

`com.shiyu.ai.agent`、`com.shiyu.ai.agent.implementation`、`com.shiyu.ai.agent.implementation.builder`、`com.shiyu.ai.agent.implementation.cache`、`com.shiyu.ai.agent.implementation.checkpoint`、`com.shiyu.ai.agent.implementation.config`、`com.shiyu.ai.agent.implementation.domain`、`com.shiyu.ai.agent.implementation.evaluation` 等（共 22 个包根）

## 内部模块依赖

`shiyu-agent-contract`、`shiyu-shared-kernel`、`shiyu-common-core`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-model-contract`、`shiyu-knowledge-contract`、`shiyu-memory-contract`、`shiyu-tooling-contract`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/agent/shiyu-agent-implementation -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
