# shiyu-tooling-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-tooling-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现工具注册与执行、MCP 工具管理、插件生命周期和插件市场，并将不可信执行交给受控运行边界。

## 功能说明

- 工具目录与执行：注册工具 provider，提供 MCP 工具列表、详情、类别、统计及执行入口。
- 插件生命周期：扫描插件、注册/启动/停止/卸载，并管理插件市场条目发布与停用。
- 插件市场适配：市场契约与存储相分离；当前实现包含内存及 JDBC 适配，市场能力受当前部署和配置约束。
- 安全与隔离：维护安全扫描、可信发布者登记和沙箱策略，并通过 Worker RPC client 与隔离执行端协作；本模块不应把插件代码作为可信本地业务代码直接运行。
- Web 与配置：Tooling Controller 暴露工具/插件/MCP 管理接口；`shiyu.plugin.enabled` 控制插件能力是否装配，缺省时按现有配置启用。
- Agent 等调用方通过 `shiyu-tooling-contract` 执行工具，不直接依赖插件存储和 worker client。

## 边界

插件执行是受安全策略和运行环境约束的能力；contract 只提供工具调用边界，不承诺每个插件 provider 或 Worker 后端都已部署。

## 主要包

`com.shiyu.ai.tooling.implementation`、`com.shiyu.ai.tooling.implementation.plugin`、`com.shiyu.ai.tooling.implementation.tool`、`com.shiyu.ai.tooling.implementation.web`

## 内部模块依赖

`shiyu-tooling-contract`、`shiyu-common-foundation`、`shiyu-common-web`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/tooling/shiyu-tooling-implementation -am test -Ddependency-check.skip=true`
