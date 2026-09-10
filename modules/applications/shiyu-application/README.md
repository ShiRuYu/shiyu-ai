# shiyu-application 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-application`
- **分类**：应用模块
- **源码规模**：生产 Java 6 个，测试 Java 4 个

## 作用

负责把各领域实现、数据库初始化、治理和保留策略组装成可运行应用。

## 职责

- 负责把各领域实现、数据库初始化、治理和保留策略组装成可运行应用。
- 数据库初始化器在 H2 上幂等安装 v4 基线，在 MySQL/PostgreSQL 上只校验 provider、版本、种子和表集合；外部库迁移由 `scripts/database` 负责。
- 只负责应用组合、入口适配或运行配置，不承载其他领域的核心业务规则。
- 通过 Maven 依赖显式装配所需领域实现。

## 边界

应用模块位于领域实现之上，是运行入口和组合位置。

## 主要包

`com.shiyu.ai.composition.database`、`com.shiyu.ai.composition.integration.governance`、`com.shiyu.ai.composition.retention`

## 内部模块依赖

`shiyu-iam-implementation`、`shiyu-agent-implementation`、`shiyu-conversation-implementation`、`shiyu-model-implementation`、`shiyu-memory-implementation`、`shiyu-knowledge-implementation`、`shiyu-tooling-implementation`、`shiyu-governance-implementation`、`shiyu-education-implementation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-application -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
