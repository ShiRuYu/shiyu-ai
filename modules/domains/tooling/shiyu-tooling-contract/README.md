# shiyu-tooling-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-tooling-contract`
- **分类**：领域模块 · Contract

## 作用

定义调用方执行已注册工具时使用的最小公共接口，不携带插件市场、MCP 或沙箱的具体实现。

## 契约内容

- `ToolService` 定义按名称执行工具的服务边界，供 Agent 等调用方通过编译期契约调用工具；工具发现和目录管理不在这个接口中。
- 本模块不注册工具、不启动插件、不运行 MCP 服务、不连接 Worker，也不定义插件市场模型。
- 契约不暴露数据库、Web 框架和沙箱实现类型。

## 调用约定

调用方以工具名称和参数 Map 调用 `ToolService.execute`，得到包含成功标志、结果和错误信息的 `ToolExecutionResult`。这个端口只表达一次工具执行的结果，不保证目标工具已注册，也不承诺插件执行环境已经部署；注册表、权限检查和 Worker RPC 都由 tooling implementation 负责。

Agent 等消费者应依赖本 contract，并在执行前完成自身的用户授权与工具审批流程，不能通过直接调用插件管理器绕过这些边界。

## 使用前提与示例

- **前提**：调用方依赖工具 contract；运行时需装配工具实现并注册相应名称的工具。工具名称和参数必须经过调用方权限及输入校验。
- **使用**：Agent 注入 `ToolService`，调用 `execute(toolName, arguments)` 得到 `ToolExecutionResult`，再将结果送回图执行流程；插件安装、启停和 MCP 连接管理由工具实现的管理入口处理。
- **限制**：`ToolService` 不是工具发现接口；不能仅凭“工具名称存在”推断当前主体有权执行。

## 边界

Agent 等工具消费者可依赖本模块；本模块不依赖 tooling implementation。

## 主要包

`com.shiyu.ai.tooling.contract`、`com.shiyu.ai.tooling.contract.api`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/tooling/shiyu-tooling-contract -am test '-Ddependency-check.skip=true'`
