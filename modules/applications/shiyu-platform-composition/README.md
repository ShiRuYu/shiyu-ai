# shiyu-platform-composition 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-platform-composition`
- **分类**：平台组合库（不可独立启动）
- **源码规模**：生产 Java 6 个，测试 Java 4 个

## 作用

负责为唯一可执行的 `shiyu-ai-bootstrap` 提供平台组合、数据库初始化、治理和保留策略。

## 职责

- 数据库初始化器在 H2 上幂等安装 v4 基线，在 MySQL/PostgreSQL 上只校验 provider、版本、种子和表集合；外部库迁移由 `scripts/database` 负责。
- 只负责应用组合、入口适配或运行配置，不承载其他领域的核心业务规则。
- 通过 Maven 依赖显式装配所需领域实现。

## 边界

组合库位于领域实现之上，为运行入口提供组合能力；本模块不包含 `main` 方法，也不生成可执行 Boot 包。

## 主要包

`com.shiyu.ai.composition.database`、`com.shiyu.ai.composition.integration.governance`、`com.shiyu.ai.composition.retention`

## 内部模块依赖

`shiyu-iam-implementation`、`shiyu-agent-implementation`、`shiyu-conversation-implementation`、`shiyu-model-implementation`、`shiyu-memory-implementation`、`shiyu-knowledge-implementation`、`shiyu-tooling-implementation`、`shiyu-governance-implementation`；可选业务实现由 bootstrap 的模块依赖提供。

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-platform-composition -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
