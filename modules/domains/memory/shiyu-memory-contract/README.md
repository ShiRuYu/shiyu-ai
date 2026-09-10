# shiyu-memory-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-memory-contract`
- **分类**：领域模块 · Contract
- **源码规模**：生产 Java 13 个，测试 Java 0 个

## 作用

Memory 记忆契约模块，定义跨模块可依赖的稳定 API、模型和 SPI 边界。

## 职责

- 负责 MAGMA 长期记忆、记忆检索、持久化和来源关联。
- 只放框架无关的公共契约，避免把数据库、Web 框架或具体供应商实现泄漏到契约层。
- 为对应 implementation 模块和组合根提供编译期依赖边界。

## 边界

下游模块可以依赖本模块；本模块不依赖同一领域 implementation。

## 主要包

`com.shiyu.ai.memory.contract`、`com.shiyu.ai.memory.contract.api`、`com.shiyu.ai.memory.contract.model`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/memory/shiyu-memory-contract -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
