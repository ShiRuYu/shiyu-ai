# shiyu-common-core 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-core`
- **分类**：基础设施模块
- **源码规模**：生产 Java 75 个，测试 Java 5 个

## 作用

提供异常、分页、响应对象、校验、工具、JDBC 方言、事件总线和基础配置等公共能力。

## 职责

- 提供异常、分页、响应对象、校验、工具、JDBC 方言、事件总线和基础配置等公共能力。
- 事件 provider 支持进程内、PostgreSQL outbox 和可选 Kafka；Kafka relay 提供重试、死信主题和 inbox 去重。
- 保持与业务领域解耦，通过稳定接口为多个领域提供横切能力。
- 避免把具体业务用例、领域实体或领域数据库表放入公共基础设施。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.core`、`com.shiyu.ai.common.core.api`、`com.shiyu.ai.common.core.config`、`com.shiyu.ai.common.core.domain`、`com.shiyu.ai.common.core.enums`、`com.shiyu.ai.common.core.exception`、`com.shiyu.ai.common.core.factory`、`com.shiyu.ai.common.core.manager` 等（共 13 个包根）

## 内部模块依赖

无内部 Maven 模块依赖

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-core -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
