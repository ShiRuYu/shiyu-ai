# shiyu-tooling-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-tooling-contract`
- **分类**：领域模块 · Contract

## 作用

定义调用方执行已注册工具时使用的最小公共接口，不携带插件市场、MCP 或沙箱的具体实现。

## 契约内容

- `ToolService` 定义发现/执行工具能力的服务边界，供 Agent 等调用方通过编译期契约调用工具。
- 本模块不注册工具、不启动插件、不运行 MCP 服务、不连接 Worker，也不定义插件市场模型。
- 契约不暴露数据库、Web 框架和沙箱实现类型。

## 边界

Agent 等工具消费者可依赖本模块；本模块不依赖 tooling implementation。

## 主要包

`com.shiyu.ai.tooling.contract`、`com.shiyu.ai.tooling.contract.api`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/tooling/shiyu-tooling-contract -am test -Ddependency-check.skip=true`
