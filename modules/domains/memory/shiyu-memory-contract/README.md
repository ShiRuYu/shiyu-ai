# shiyu-memory-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-memory-contract`
- **分类**：领域模块 · Contract

## 作用

定义长期记忆摄取、查询、图路径和治理状态所需的协作类型，供 Agent 等模块调用。

## 契约内容

- `MemoryIngestionPort` 与摄取命令表达记忆写入入口；`MemoryQueryPort` 与查询模型描述检索请求、结果和关系路径。
- 模型包括记忆事件、实体/边及图关系、状态和确认策略，供调用者解释记忆来源与治理状态。
- 契约描述调用与数据含义，不实现 MAGMA 解析、存储、索引、合并或检索算法。
- 保持框架无关，不暴露数据库、向量引擎或 Web DTO。

## 边界

Agent 等需要记忆协作的模块可依赖该契约；本模块不依赖 memory implementation。

## 主要包

`com.shiyu.ai.memory.contract`、`com.shiyu.ai.memory.contract.api`、`com.shiyu.ai.memory.contract.model`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/memory/shiyu-memory-contract -am test -Ddependency-check.skip=true`
