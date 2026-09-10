# shiyu-ai-bootstrap 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-ai-bootstrap`
- **分类**：应用模块
- **源码规模**：生产 Java 5 个，测试 Java 4 个

## 作用

负责 Spring Boot 启动、运行时锁、保留策略、启动事件和应用集成测试。

## 职责

- 负责 Spring Boot 启动、运行时锁、保留策略、启动事件和应用集成测试。
- 只负责应用组合、入口适配或运行配置，不承载其他领域的核心业务规则。
- 通过 Maven 依赖显式装配所需领域实现。

## 边界

应用模块位于领域实现之上，是运行入口和组合位置。

## 主要包

`com.shiyu.ai.bootstrap`、`com.shiyu.ai.bootstrap.lifecycle`、`com.shiyu.ai.bootstrap.lock`、`com.shiyu.ai.bootstrap.retention`

## 内部模块依赖

`shiyu-application`、`shiyu-governance-implementation`、`shiyu-ai-web`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-bootstrap -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
