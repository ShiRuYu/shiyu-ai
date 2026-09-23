# shiyu-common-vector 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-vector`
- **分类**：基础设施模块

## 作用

定义向量存储接口，并为进程内、JVector 和 PostgreSQL pgvector 提供可选适配实现。

## 功能说明

- `VectorStore` 抽象向量插入、按标识查询/删除、相似度搜索、过滤、计数与索引维护操作；领域模块通过接口使用，而不依赖具体引擎类。
- 工厂根据 provider 配置选择 `inmemory`、`jvector` 或 `pgvector` 实现；`shiyu.infrastructure.vector.provider` 优先于兼容配置 `shiyu.vector-store.type`。
- pgvector provider 需要 PostgreSQL、`vector` 扩展及匹配的向量维度；JVector 与进程内 provider 的生命周期、持久性各有差异，需依据部署配置选择。
- 自动配置的默认 `VectorStore` 使用 `global/default` 命名空间，这是共享索引空间而非租户隔离保证；租户数据应由上层通过 provider 打开租户/业务空间，并提供正确过滤条件。
- 更换 provider 或嵌入模型会影响已有向量的可用性，相关知识索引应按知识模块流程重建。本模块不包含知识文档、分块或嵌入业务流程。

## Provider 与索引生命周期

- `VectorStoreFactory` 根据 `VectorStoreProperties` 与 `VectorInfrastructureProperties` 选择 `InMemoryVectorStore`、`JVectorStore` 或 `PgVectorStore`；`VectorStoreAutoConfiguration` 只装配默认实例。
- 写入方提供向量、标识和命名空间；查询方通过 `VectorSearchRequest` 指定相似度搜索及过滤条件。`VectorStore` 的技术接口不判断知识空间成员权限。
- `InMemoryVectorStore` 的数据随进程消失；JVector 使用本地目录；pgvector 使用 PostgreSQL。迁移 provider 时必须重建或迁移索引，不能仅改变配置键。
- 默认 `global/default` 实例不适合直接保存多租户业务索引；知识/记忆模块必须在上层构造稳定的租户与业务命名空间。

本模块解决“向量存在哪里、如何检索”，不解决“哪些文档应入库、哪个用户可读取命中结果”。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.vector.api`、`com.shiyu.ai.common.vector.config`、`com.shiyu.ai.common.vector.factory`、`com.shiyu.ai.common.vector.implementation`、`com.shiyu.ai.common.vector.model`

## 内部模块依赖

`shiyu-common-foundation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-vector -am test -Ddependency-check.skip=true`
