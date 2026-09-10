# shiyu-common-web 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-web`
- **分类**：基础设施模块
- **源码规模**：生产 Java 13 个，测试 Java 2 个

## 作用

提供认证适配、异常处理、文件、过滤器、拦截器和参数校验。

## 职责

- 提供认证适配、异常处理、文件、过滤器、拦截器和参数校验。
- 保持与业务领域解耦，通过稳定接口为多个领域提供横切能力。
- 避免把具体业务用例、领域实体或领域数据库表放入公共基础设施。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.web.auth`、`com.shiyu.ai.common.web.exception`、`com.shiyu.ai.common.web.file`、`com.shiyu.ai.common.web.filter`、`com.shiyu.ai.common.web.interceptor`、`com.shiyu.ai.common.web.servlet`、`com.shiyu.ai.common.web.validation`

## 内部模块依赖

`shiyu-common-core`、`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-web -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
