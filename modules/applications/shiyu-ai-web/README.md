# shiyu-ai-web 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-ai-web`
- **分类**：应用模块
- **源码规模**：生产 Java 13 个，测试 Java 11 个

## 作用

负责对外 REST/SSE 接口、安全配置、认证上下文和 Web 层适配。

## 职责

- 负责对外 REST/SSE 接口、安全配置、认证上下文和 Web 层适配。
- 只负责应用组合、入口适配或运行配置，不承载其他领域的核心业务规则。
- 通过 Maven 依赖显式装配所需领域实现。

## 边界

应用模块位于领域实现之上，是运行入口和组合位置。

## 主要包

`com.shiyu.ai.web.auth`、`com.shiyu.ai.web.common`、`com.shiyu.ai.web.config`、`com.shiyu.ai.web.config.properties`、`com.shiyu.ai.web.interceptor`

## 内部模块依赖

`shiyu-shared-kernel`、`shiyu-common-web`、`shiyu-iam-implementation`、`shiyu-agent-implementation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-web -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
