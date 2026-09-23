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

## 组合与数据链路

- `PlatformCompositionAutoConfiguration` 是应用组合根，导入平台配置并扫描 Web 入口；它把各领域实现接入同一 Spring 应用，但不把领域类型搬进组合模块。
- `DatabaseInitializer` 读取仓库内 H2 基线资源，检查数据库是否已经初始化并按版本安装种子；外部 MySQL/PostgreSQL 需要先执行独立迁移，再由应用校验结果。
- `QuotaGenerationAdmission` 将会话生成准入接到治理配额契约；`ConversationUsageSink` 与 `UsageEventListener` 把生成/模型用量交给治理记录链路。
- `DataRetentionService` 读取 `DataRetentionProperties` 对受保留策略约束的数据执行清理；它不是数据库备份或迁移工具。

该模块只负责跨模块连线和生命周期编排。单一领域内的策略、Repository 与 HTTP Controller 留在对应 implementation。

## 使用前提与示例

- **前提**：作为库随 Bootstrap 装配，不能单独运行；H2 可由 `DatabaseInitializer` 安装基线，MySQL/PostgreSQL 要先按 `scripts/database` 准备和迁移，再启动应用校验。
- **使用**：新增跨领域协作时在组合层提供桥接 Bean，例如将 conversation 的 `GenerationAdmission` 接到 governance 的 `QuotaGovernance`；领域实现只依赖对方 contract。数据保留通过 `shiyu.retention.data` 配置，由 `DataRetentionService` 执行。
- **限制**：组合层负责连接，不应承载领域规则；数据库初始化也不能代替外部数据库迁移或备份。

## 边界

组合库位于领域实现之上，为运行入口提供组合能力；本模块没有 `main` 方法，不单独监听端口，也不生成可执行 Boot 包。

## 主要包

`com.shiyu.ai.composition.database`、`com.shiyu.ai.composition.integration.governance`、`com.shiyu.ai.composition.retention`

## 内部模块依赖

`shiyu-agent-implementation`、`shiyu-common-mybatis`、`shiyu-conversation-implementation`、`shiyu-education-implementation`、`shiyu-governance-implementation`、`shiyu-iam-implementation`、`shiyu-knowledge-implementation`、`shiyu-memory-implementation`、`shiyu-model-implementation`、`shiyu-tooling-implementation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-platform-composition -am test '-Ddependency-check.skip=true'`
