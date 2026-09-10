# shiyu-shared-kernel 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-shared-kernel`
- **分类**：共享内核模块
- **源码规模**：生产 Java 11 个，测试 Java 5 个

## 作用

提供多个有界上下文共享的框架无关原语。

## 职责

- 提供上下文标识、错误模型、事件和分页等稳定基础类型。
- 只收纳跨领域且变化频率低的概念，禁止演变成业务杂物包。
- 不依赖任何领域 implementation 或应用入口。

## 边界

所有领域 contract 可以依赖；本模块保持无 Spring、数据库和业务实现耦合。

## 主要包

`com.shiyu.ai.kernel.context`、`com.shiyu.ai.kernel.error`、`com.shiyu.ai.kernel.event`、`com.shiyu.ai.kernel.page`

## 内部模块依赖

无内部 Maven 模块依赖

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/shared/shiyu-shared-kernel -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
