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

## 工具和插件的不同链路

- 普通工具：`ToolServiceImpl` 维护工具注册与执行入口，`McpToolController` 暴露管理/调用接口；Agent 经 tooling contract 按工具名称与参数发起执行。
- MCP 接入：`McpToolAutoConfiguration` 注册已装配的工具，具体可用性仍取决于当前应用启用的 provider 与工具配置。
- 插件生命周期：`PluginManager` 负责扫描、启动、停止和卸载，`PluginController` 提供管理入口；安全扫描、签名及发布者策略在进入执行边界前应用。
- 插件市场：`PluginMarketService` 面向 `PluginMarketStore`，可选 `JdbcPluginMarketStore` 或 `InMemoryPluginMarketStore`，并不保证两者具有相同的重启持久性。
- 不可信执行：Worker RPC client 将插件执行交给隔离端，管理进程不应直接把上传插件代码当成可信组件装配。

工具目录、插件市场和插件执行安全是不同功能。打开 `shiyu.plugin.enabled` 只启用相关装配条件，不代表外部 Worker、签名信任链或市场数据已完成部署。

## 使用前提与示例

- **前提**：普通工具需注册工具 provider；插件能力由 `shiyu.plugin.enabled` 控制，外部 Worker、信任发布者与沙箱策略需按部署场景另行准备。启用开关本身不会创建安全执行环境。
- **使用**：Agent 通过 contract 的 `ToolService.execute(toolName, parameters)` 调用已授权工具；管理端使用 `/api/tooling/tools/mcp` 查看或调用 MCP 工具，使用 `/api/tooling/plugins` 管理插件生命周期。
- **注意**：插件扫描、市场上架、启动与执行是不同步骤；不可信插件不能直接以应用内 Bean 运行，执行前仍需权限、参数、签名/安全策略检查。

## 边界

插件执行是受安全策略和运行环境约束的能力；contract 只提供工具调用边界，不承诺每个插件 provider 或 Worker 后端都已部署。

## 主要包

`com.shiyu.ai.tooling.implementation`、`com.shiyu.ai.tooling.implementation.plugin`、`com.shiyu.ai.tooling.implementation.tool`、`com.shiyu.ai.tooling.implementation.web`

## 内部模块依赖

`shiyu-tooling-contract`、`shiyu-common-foundation`、`shiyu-common-web`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/tooling/shiyu-tooling-implementation -am test '-Ddependency-check.skip=true'`
