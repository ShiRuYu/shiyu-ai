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

## 写入与查询协议

`MemoryIngestionPort` 接收 `IngestMemoryCommand`，把外部事件转为可管理的记忆写入；`MemoryQueryPort.retrieve` 根据 `MemoryQuery` 返回 `MemoryPath` 列表，调用方可沿路径理解实体和关系。`MemoryEventStatus`、`ConfirmationPolicy`、`GraphType` 和 `EdgeOrigin` 分别表达事件治理状态、确认要求、图类型与边的来源。

这些模型没有承诺特定解析器、数据库或向量相似度算法。Agent 只应依据 contract 消费检索结果；记忆确认、替代与撤销的规则归 memory implementation 管理。

## 使用前提与示例

- **前提**：调用方依赖记忆 contract；运行时装配 MAGMA 实现、数据库/向量存储以及需要的模型嵌入能力。请求的租户和主体来自可信上下文。
- **使用**：生成过程通过 `MemoryIngestionPort` 提交摄取命令；组装下一轮上下文时调用 `MemoryQueryPort.retrieve` 获取候选记忆与关系路径。业务模块不直接操作记忆索引表或向量引擎。
- **权限**：接入对象级权限策略后再使用共享记忆；仅有租户隔离并不等于所有记忆对象都对租户内用户可见。

## 边界

Agent 等需要记忆协作的模块可依赖该契约；本模块不依赖 memory implementation。

## 主要包

`com.shiyu.ai.memory.contract`、`com.shiyu.ai.memory.contract.api`、`com.shiyu.ai.memory.contract.model`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/memory/shiyu-memory-contract -am test '-Ddependency-check.skip=true'`
