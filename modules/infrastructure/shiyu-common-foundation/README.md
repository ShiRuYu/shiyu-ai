# shiyu-common-foundation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-foundation`
- **分类**：基础设施模块

## 作用

集中放置被多个应用和领域复用的平台基础设施原语；它是带 Spring/JDBC 等基础技术适配的公共模块，不是无框架的领域核心。

## 功能说明

- 提供 API 结果、分页与筛选/排序模型、公共异常和校验配置，供 Web 与应用服务复用。
- 提供事务上下文及提交/回滚钩子、JDBC 方言识别和数据库基线贡献接口，支持公共基础设施协作。
- 提供模块条件判断与通用 JSON、字符串等工具；业务模块开关通过环境属性读取，不依赖总模块属性对象。
- 认证抽象与公共上下文访问适配依赖 `shared-kernel` 的类型；稳定身份、租户值和领域事件契约仍属于 shared-kernel。
- 事件发布、outbox 和 Kafka relay 位于可选的 `shiyu-common-event`；日志实现由可执行应用选型。本模块不放领域用例、业务实体或领域数据库表。

## 公共能力与使用方式

| 能力 | 主要类型 | 由谁使用 |
| --- | --- | --- |
| 统一响应和异常 | `Result`、公共异常及分页/排序模型 | Web 入口和应用服务统一表达处理结果，不在此决定领域错误码的业务含义。 |
| 事务后协作 | `TransactionContext`、`TransactionHookExecutor`、`TransactionTemplateExecutor` | 需要提交/回滚钩子或显式事务边界的基础设施和领域实现。 |
| 数据库通用协议 | `JdbcDialect`、数据库基线贡献接口 | 数据库初始化与 JDBC 适配器处理方言差异和资源归属。 |
| 条件化装配 | `BusinessModuleCondition`、`ShiYuProperties`、`AppHomeEnvironmentPostProcessor` | 应用读取模块开关与运行目录配置；本模块不直接启动任何业务模块。 |
| 校验与工具 | `ValidatorConfig`、JSON/字符串工具 | 多模块复用的技术性校验和转换。 |

`common-foundation` 允许 Spring/JDBC 等基础技术依赖，不能被误当成纯领域内核；需要在 contract 中共享的无框架标识优先放在 `shiyu-shared-kernel`。

## 使用前提与示例

- **前提**：需要 Spring 事务、JDBC、校验等平台能力的模块才引入 `shiyu-common-foundation`；无框架的跨领域值类型应优先放在 `shiyu-shared-kernel`。
- **使用**：业务服务通过公共异常/响应约定向 Web 层表达失败，需新事务边界时注入 `TransactionTemplateExecutor` 并调用 `executeNew(() -> operation())`；模块开关由 `BusinessModuleCondition` 读取 `shiyu.modules.<id>.enabled`。
- **限制**：`executeNew` 会新建事务，不应被当作普通方法包装器；本模块不提供业务 Repository，也不会因为引入依赖而自动启用业务模块。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`api`、`auth`、`context`、`context.model`、`config`、`database`、`domain`、`exception`、`jdbc`、`module`、`tx`、`utils`、`validate`、`vo` 等。认证接口位于 `auth`，身份上下文访问位于 `context`，上下文模型位于 `context.model`；`domain` 仅保留公共实体基类。

## 内部模块依赖

依赖 `shiyu-shared-kernel`。

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-foundation -am test '-Ddependency-check.skip=true'`

事件能力需显式依赖 `shiyu-common-event` 才会装配。
