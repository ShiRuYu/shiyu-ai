# shiyu-memory-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-memory-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现 MAGMA 长期记忆的写入、图关系维护、检索与治理，并适配 Agent 的上下文检索接口。

## 功能说明

- 记忆摄取：将事件和来源材料解析为实体与关系，执行实体解析和时间/语义/因果关系归并。
- 图与检索：维护 MAGMA 实体、边和图类型，按请求检索记忆并生成可追踪的检索结果/轨迹。
- 生命周期治理：支持确认、撤销、替代和整合流程，并维护索引及关系贡献能力。
- Agent 集成：通过 ContextRetrieval adapter 将记忆检索提供给 Agent 上下文装配；对外业务接口通过 Memory contract 暴露。
- 持久化与向量能力：记忆元数据由领域持久化层管理，语义索引使用 common-vector；H2 schema/seed 支持本地开发验证。
- 本模块实现记忆领域而非通用 Agent runtime、向量引擎或模型调用服务。

## 边界

其他模块依赖 `shiyu-memory-contract`；检索结果可追溯性与记忆确认/撤销状态由本模块维护，调用方不应直接操作存储实体。

## 主要包

`com.shiyu.ai.memory.implementation`、`com.shiyu.ai.memory.implementation.domain`、`com.shiyu.ai.memory.implementation.persistence`、`com.shiyu.ai.memory.implementation.web`

## 内部模块依赖

`shiyu-memory-contract`、`shiyu-shared-kernel`、`shiyu-model-contract`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-common-vector`、`shiyu-agent-contract`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/memory/shiyu-memory-implementation -am test -Ddependency-check.skip=true`
