# shiyu-architecture-tests 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-architecture-tests`
- **分类**：架构测试模块
- **源码规模**：生产 Java 0 个，测试 Java 4 个

## 作用

用可执行的 ArchUnit 规则验证模块、上下文和持久化边界。

## 职责

- 加载各业务模块字节码并检查依赖方向。
- 验证领域之间不直接依赖 implementation，以及 Web/持久化边界约束。
- 作为 Maven verify 和 CI 的架构回归门禁。

## 边界

测试模块只依赖被测模块，不向生产模块提供运行时能力。

## 主要包

无生产 Java 包（测试规则位于 `src/test/java`）

## 内部模块依赖

`shiyu-ai-web`、`shiyu-common-mybatis`、`shiyu-iam-implementation`、`shiyu-agent-implementation`、`shiyu-conversation-implementation`、`shiyu-model-implementation`、`shiyu-memory-implementation`、`shiyu-knowledge-implementation`、`shiyu-tooling-implementation`、`shiyu-governance-implementation`、`shiyu-education-implementation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl tests/shiyu-architecture-tests -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。
