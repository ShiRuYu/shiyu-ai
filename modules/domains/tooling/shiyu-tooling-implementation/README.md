# shiyu-tooling-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-tooling-implementation`
- **分类**：领域模块 · Implementation
- **源码规模**：生产 Java 29 个，测试 Java 13 个

## 作用

Tooling 工具实现模块，承载该领域的应用服务、领域模型和基础设施适配。

## 职责

- 负责工具注册、MCP、插件生命周期、沙箱和 Worker 执行。
- 在领域内部组织 application、domain、persistence、infrastructure 和 web 等实现职责。
- 通过 contract、shared-kernel 与公共基础设施协作，对外暴露领域服务。

## 边界

实现细节只在组合根和需要该能力的应用层装配；其他领域应依赖 contract。

## 主要包

`com.shiyu.ai.tooling.implementation`、`com.shiyu.ai.tooling.implementation.plugin`、`com.shiyu.ai.tooling.implementation.tool`、`com.shiyu.ai.tooling.implementation.web`

## 内部模块依赖

`shiyu-tooling-contract`、`shiyu-common-core`、`shiyu-common-web`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/tooling/shiyu-tooling-implementation -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
