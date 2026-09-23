# shiyu-platform-composition 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-platform-composition`
- **分类**：平台组合库（不可独立启动）

## 作用

为唯一可执行的 `shiyu-ai-bootstrap` 提供平台级自动配置、数据库基线协调、用量事件处理和数据保留能力。

## 功能说明

- `PlatformCompositionAutoConfiguration` 组合平台级组件，并连接显式纳入应用的领域实现；业务模块启用条件仍由各模块配置和 `shiyu.modules.*.enabled` 控制。
- `DatabaseInitializer` 在 H2 环境幂等安装仓库提供的基线；连接 MySQL/PostgreSQL 时校验 provider、版本、种子和表集合，不代替 `scripts/database` 执行外部数据库迁移。
- `UsageEventListener` 接收治理用量事件并交给平台侧处理；`DataRetentionService` 执行按配置定义的数据保留工作。
- 测试与资源包含 H2 平台基线；本模块不拥有 IAM、Agent、知识等领域业务规则。

## 边界

组合库位于领域实现之上，为运行入口提供组合能力；本模块没有 `main` 方法，不单独监听端口，也不生成可执行 Boot 包。

## 主要包

`com.shiyu.ai.composition.database`、`com.shiyu.ai.composition.integration.governance`、`com.shiyu.ai.composition.retention`

## 内部模块依赖

`shiyu-agent-implementation`、`shiyu-common-mybatis`、`shiyu-conversation-implementation`、`shiyu-education-implementation`、`shiyu-governance-implementation`、`shiyu-iam-implementation`、`shiyu-knowledge-implementation`、`shiyu-memory-implementation`、`shiyu-model-implementation`、`shiyu-tooling-implementation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-platform-composition -am test -Ddependency-check.skip=true`
