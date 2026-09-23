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

## MAGMA 处理路径

1. `MagmaMemoryService` 接收摄取/查询请求，`MemoryEntityResolver` 识别可复用实体，领域模型记录实体、事件、关系和检索轨迹。
2. `JdbcMagmaMemoryRepository` 存储记忆实体与关系，`JVectorMemorySemanticIndex` 提供语义检索适配；数据库事实与可重建索引不是同一份状态。
3. `MemoryAccessPolicy` 可限制可读范围，`MemoryRetrievalPolicyProvider` 控制召回策略；确认、撤销、替代等治理状态由 `MemoryGovernancePort` 等端口协作维护。当前 `MagmaMemoryConfiguration` 未取得策略 Bean 时会使用放行策略，因此部署方不能把该扩展点本身当作已生效的对象授权。
4. `MagmaConsolidationWorker` 执行记忆整合任务，`MagmaContextRetrievalAdapter` 把授权后的记忆路径转换为 Agent 可用上下文。

记忆摄取不等于立即可信。调用方需要区分原始事件、待确认事实和治理后的记忆；异步整合与向量索引不能绕过租户与主体访问策略。

## 使用前提与示例

- **前提**：记忆表、向量索引和嵌入服务已装配；任务输入包含可信租户与主体。需要对象级可见性时必须额外提供 `MemoryAccessPolicy` Bean，因为缺失时当前配置会采用放行策略。
- **使用**：Agent/会话侧通过 `MemoryIngestionPort` 摄取事件，通过 `MemoryQueryPort.retrieve` 查询候选记忆；也可使用 `/api/memory` 下的管理入口处理实体与关系。长期整合由 `MagmaConsolidationWorker` 执行，查询结果包含可追踪路径。
- **注意**：新摄取的材料与已确认记忆不能混为一谈；向量命中不是权限证明，跨线程整合须重新绑定租户作用域。

## 边界

其他模块依赖 `shiyu-memory-contract`；检索结果可追溯性与记忆确认/撤销状态由本模块维护，调用方不应直接操作存储实体。

## 主要包

`com.shiyu.ai.memory.implementation`、`com.shiyu.ai.memory.implementation.domain`、`com.shiyu.ai.memory.implementation.persistence`、`com.shiyu.ai.memory.implementation.web`

## 内部模块依赖

`shiyu-memory-contract`、`shiyu-shared-kernel`、`shiyu-model-contract`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-common-vector`、`shiyu-agent-contract`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/memory/shiyu-memory-implementation -am test '-Ddependency-check.skip=true'`
