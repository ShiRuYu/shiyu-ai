# 后端包边界审计

本页是 `2026-09-09-package-boundaries.md` 的落地记录。生产 Java 文件的逐文件归属以 [`package-migration.csv`](../../scripts/architecture/package-migration.csv) 为准；本页只记录非领域模块的能力边界和有意保留的兼容包名。

## 非领域模块归属

| 模块 | 当前能力包 | 边界决策 |
| --- | --- | --- |
| `shiyu-shared-kernel` | `context`、`error`、`event`、`page` | 只放跨领域身份、上下文、错误、事件和分页值对象；不放 Spring、Servlet、ORM 或领域实现。 |
| `shiyu-common-core` | `api`、`config`、`database`、`domain`、`enums`、`event`、`exception`、`factory`、`jdbc`、`manager`、`service`、`tx`、`utils`、`validate`、`vo` | 作为兼容性基础设施模块保留已有公共 FQCN。`domain` 是认证/上下文适配，不是业务领域；`utils` 只承载已有跨模块工具，本次不新增收容类。文件级用途和消费者由清单锁定。 |
| `shiyu-common-mybatis` | `config`、`datasource`、`handler`、`mapper`、`model` | 只提供数据源、租户/异常处理、Mapper 和持久化基础模型。 |
| `shiyu-common-web` | `auth`、`config`、`exception`、`file`、`filter`、`interceptor`、`servlet`、`validation` | 只提供可复用 HTTP 适配；领域 Controller 不进入该模块。 |
| `shiyu-common-storage` | `api`、`backup`、`config`、`file`、`idempotency`、`lease`、`metadata`、`rate`、`security`、`vector`、`web` | 文件、备份、元数据、租约、限流和上传适配按技术能力隔离；领域规则通过 contract 进入。 |
| `shiyu-common-thread` | `api`、`config`、`context`、`executor`、`metrics`、`otel` | 扩展接口、执行器、上下文传播和可选观测分层。 |
| `shiyu-common-vector` | `api`、`config`、`factory`、`implementation`、`model` | 搜索契约、模型、后端实现、工厂和自动装配分开；不与 storage 文件索引混合。 |
| `shiyu-application` | `composition.database`、`composition.integration.governance`、`composition.retention` | 只编排初始化、跨领域接线和保留策略，不承载领域规则。 |
| `shiyu-ai-web` | `config`、`auth`、`interceptor`、`exception` | 入口层只做通用装配；Agent/Education 的领域 Web 配置由各自实现模块通过 SPI 注入。 |
| `shiyu-ai-bootstrap` | `bootstrap`、`bootstrap.lifecycle`、`bootstrap.lock`、`bootstrap.retention` | 负责可执行应用生命周期、APP_HOME 锁和日志清理。 |
| `shiyu-platform-bootstrap` | `platform.bootstrap` | 只装配平台能力，并通过依赖排除保证不携带 Education business implementation。 |
| `shiyu-architecture-tests` | `architecture` | Module、Context、Persistence 三类 ArchUnit 规则独立维护。 |

## 检查职责

- `inventory_java_packages.py` 负责生成清单、检测路径不一致、重复 FQCN、split-package、跨模块 import、contract 外部依赖和 wildcard/static import 人工核查项。
- `check_domain_module_dependencies.py` 负责 Maven 实现依赖、实现类直接 import、contract import 实现类、contract 框架依赖、实际 Web POM 和空扫描检查。
- `scripts/docs` 只负责文档/路由/schema 清单；`scripts/continuous_testing` 保持停用，不启动守护进程。

`scripts/continuous_testing` 内部仍按 `state/store`、`scheduler/queue`、`runner/process_control`、`gates`、`report`、`retention` 和 `worktree` 分工；本次只做源码和文档审计，未启动 daemon、计划任务或后台服务。

## 当前证据

最近一次清单和门禁结果：生产 Java `1183` 个，split-package `0`，重复 FQCN `0`，路径不一致 `0`；Python 架构夹具 `18` 个测试通过；严格模式 `mvn clean verify` 的 `31` 个 Maven 项目全部通过。前端 Education/Agent API 回归为 `10` 个测试通过，TypeScript 检查通过。
