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

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.vector.api`、`com.shiyu.ai.common.vector.config`、`com.shiyu.ai.common.vector.factory`、`com.shiyu.ai.common.vector.implementation`、`com.shiyu.ai.common.vector.model`

## 内部模块依赖

`shiyu-common-foundation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-vector -am test -Ddependency-check.skip=true`
