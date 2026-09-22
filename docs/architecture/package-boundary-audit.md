# 后端包边界审计

本页是 `2026-09-09-package-boundaries.md` 的落地记录。生产 Java 文件的逐文件归属以 [`package-migration.csv`](../../scripts/architecture/package-migration.csv) 为准；本页记录非领域模块的能力边界、领域职责拆分以及有意保留的聚合。

## 非领域模块归属

| 模块 | 当前能力包 | 边界决策 |
| --- | --- | --- |
| `shiyu-shared-kernel` | `context`、`error`、`event`、`page` | 只放跨领域身份、上下文、错误、事件和分页值对象；不放 Spring、Servlet、ORM 或领域实现。 |
| `shiyu-common-foundation` | `api`、`auth`、`context`、`context.model`、`config`、`database`、`domain`、`enums`、`exception`、`factory`、`jdbc`、`manager`、`module`、`service`、`tx`、`utils`、`validate`、`vo` | 登录接口、身份上下文访问和上下文模型分别归属 `auth`、`context`、`context.model`；`domain` 保留公共实体基类。事件 provider 已独立，运行时日志实现由启动应用选择。 |
| `shiyu-common-event` | `com.shiyu.ai.common.event.{api,config,publisher,outbox,support}` | 可选事件 provider 模块，按契约、配置、进程内发布、outbox 实现和支撑能力分包；默认应用不自动依赖，启用时由应用显式引入。 |
| `shiyu-common-mybatis` | `config`、`datasource`、`handler`、`mapper`、`model` | 只提供数据源、租户/异常处理、Mapper 和持久化基础模型。 |
| `shiyu-common-web` | `auth`、`config`、`exception`、`file`、`filter`、`interceptor`、`servlet`、`validation` | 只提供可复用 HTTP 适配；领域 Controller 不进入该模块。 |
| `shiyu-common-storage` | `api`、`backup`、`config`、`file`、`idempotency`、`lease`、`metadata`、`rate`、`security`、`vector`、`web` | 文件、备份、元数据、租约、限流和上传适配按技术能力隔离；领域规则通过 contract 进入。 |
| `shiyu-common-thread` | `api`、`config`、`context`、`executor`、`metrics`、`otel` | 扩展接口、执行器、上下文传播和可选观测分层。 |
| `shiyu-common-vector` | `api`、`config`、`factory`、`implementation`、`model` | 搜索契约、模型、后端实现、工厂和自动装配分开；不与 storage 文件索引混合。 |
| `shiyu-platform-composition` | `composition.database`、`composition.integration.governance`、`composition.retention` | 平台组合库，只编排初始化、跨领域接线和保留策略，不承载领域规则，也不独立启动。 |
| `shiyu-ai-web` | `config`、`auth`、`interceptor`、`exception` | 入口层只做通用装配；Agent/Education 的领域 Web 配置由各自实现模块通过 SPI 注入。 |
| `shiyu-ai-bootstrap` | `bootstrap`、`bootstrap.lifecycle`、`bootstrap.lock`、`bootstrap.retention` | 唯一可执行应用；平台能力始终装配，业务实现通过模块自动配置和开关条件加入。 |
| `shiyu-architecture-tests` | `architecture` | Module、Context、Persistence 三类 ArchUnit 规则独立维护。 |

## 检查职责

2026-09-19 职责复核后的领域包划分：

- Agent：启动执行器归入 `startup`，编辑器数据源 URL 常量归入 `web.api`。
- IAM：Sa-Token 会话存储实现归入 `infrastructure.auth`，配置包仅装配配置。
- Model：模型适配端口归入 `infrastructure.port`，模型管理服务归入 `infrastructure.service`，适配基类和实现保留在 `infrastructure.adapter`。
- Knowledge：解析、图存储、分块端口归入 `application.port`；格式解析、内存图存储和中文分块实现归入 `infrastructure` 对应能力包；图门面和文档摄取归入 `application.service`；检索服务与适配器分包。
- Conversation：`domain.chat` 下按 `model`、`codec`、`service` 分开，内部编解码结果及规划结果保留在所属实现中。
- Memory：`MemoryEntityResolver` 归入 Magma `port` 包。
- Model 补充复核：`gateway.model` 保存独立路由模型，`gateway.service` 保存路由服务；`media.port` 与 `media.service` 分离媒体契约和注册服务。
- 基础设施补充复核：租户过滤工厂归入 `common.mybatis.tenant`，文件键生成与解析归入 `common.storage.file.key`；IAM 的认证上下文查询实现归入 `application.service`。

以上迁移保持业务逻辑和显式 Bean 方法名称。包白名单不再豁免已拆分的旧混合包；小型内部结果类型仍允许与其唯一使用服务共存。

- `check_mixed_package_roles.py` 增补无注解服务、仓储实现识别；接口名含 Service/Adapter 不视作实现。该检查仍为启发式静态扫描，不能替代人工职责审计。
- 配置包内的存储实现、启动执行器和 `TenantFactory` 实现不受包级白名单豁免；未知职责仍需要人工核查，不将扫描通过解释为全部类型职责识别正确。

- `inventory_java_packages.py` 负责生成清单、检测路径不一致、重复 FQCN、split-package、跨模块 import、contract 外部依赖和 wildcard/static import 人工核查项。
- `check_lombok_state_objects.py` 检查可变 `*Properties`、请求/响应和 VO/DTO 状态对象是否使用 Lombok 访问器；记录、枚举、工具类、配置/组件和显式 allowlist 例外不纳入检查。
- `check_domain_module_dependencies.py` 负责 Maven 实现依赖、实现类直接 import、contract import 实现类、contract 框架依赖、实际 Web POM 和空扫描检查。
- `scripts/docs` 只负责文档/路由/schema 清单；`scripts/continuous_testing` 保持停用，不启动守护进程。

`scripts/continuous_testing` 内部仍按 `state/store`、`scheduler/queue`、`runner/process_control`、`gates`、`report`、`retention` 和 `worktree` 分工；本次只做源码和文档审计，未启动 daemon、计划任务或后台服务。

## 当前证据

最近一次清单和门禁结果应以提交前命令输出为准；本次新增事件模块后，`common-foundation` 与 `common-event` 分别由各自 README 和 Maven 测试命令验证。Kafka 容器测试在无 Docker 环境中标记为跳过，不等同于通过。

2026-09-19 本轮职责迁移验证：

- 生产清单为 29 个 Java 模块、1,203 个 Java 文件、331 个包；无重复 FQCN、跨模块同名包或路径错配。
- 架构脚本单元测试 71 项通过；职责扫描、跨领域依赖、租户隔离、接口注释存在性、文档和差异格式检查通过。注释检查不代表注释语义全部合格。
- 清理构建后，最终 `mvn --batch-mode --no-transfer-progress -Pstrict-warnings test -Ddependency-check.skip=true` 于 22:34 完成，31 个 reactor 模块全部成功；测试报告合计 996 项，0 失败、0 错误、5 跳过。
- 5 项跳过分别为 Kafka、MinIO、Redis、PgVector 容器测试（无 Docker）及离线嵌入模型测试（未启用离线模型）；这些环境未获本轮验证。
- 启动核心流程集成测试和 ArchUnit 边界测试通过；未修改 HTTP 协议、数据库结构或业务模块开关语义。
