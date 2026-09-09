# 后端类与包划分重构执行计划

> 执行方式：使用 executing-plans 按任务顺序实施，每项完成后检查差异和验证结果。本文件是执行计划，不代表重构已实施。

**Goal:** 统一后端全部模块的类与包边界，使领域、入口、基础设施与共享生产类归属可检查，并拆分职责过重的服务，保持 HTTP、数据库和业务行为兼容。
**Architecture:** 保留现有 Maven 叶子模块。以模块归属建立可靠检查，再把公开契约迁入 contract 命名空间，内部实现按 application/domain/infrastructure/web 分层；机械迁移与行为重构分别提交。
**Tech Stack:** Java 21、Maven、Spring Boot、JUnit、ArchUnit、Python 标准库；前端回归使用现有 pnpm/Vitest。
**Spec:** 本任务对话中的类与包分析，以及 docs/模块结构与命名.md；本计划中的目标规则是对现有混合命名的明确收敛方案。

## 约束与范围

- 范围为 shiyu-ai 的 modules、tests、相关脚本、配置和文档；前端仅做接口兼容回归。
- 持续测试已被用户要求停止，本计划不恢复守护进程、不增加自动任务。
- 保留模块目录和 artifactId，避免目录、坐标、包名同时变化。
- Java 包迁移会改变全限定类名；必须更新 import、反射、扫描、MyBatis、SPI、序列化类型名及测试引用。不得宣称类路径不变。
- HTTP 路由、JSON 字段、错误码、授权结果、事务语义、表名和 schema 保持兼容。
- 不更换框架版本，不顺带升级依赖，不将全部控制器集中回公共入口模块。
- 每个阶段一个可审查提交；主分支最终按用户减少记录的偏好进行 squash。保留原分支，不自动强推主分支。
- 规划阶段只编写本文件；实施时建立隔离分支，完成验证后再集成。

## 当前依据和基线

最近源码清点：1138 个生产 Java 文件；领域实现模块 878 个，包名含 implementation 的仅 159 个。该统计是源文件数，不是类覆盖率。
已确认同名包横跨契约和实现：agent.node、agent.node.creator、runtime、knowledge.dto、knowledge.retrieval、memory.magma、model.chat、tool。
ArchUnit 的 implementation 和 contract 包匹配无法覆盖全部模块内容；现有 Python 模块依赖检查需要联合审查，不能从一条规则推断所有约束失效。
本次读取发现 check_domain_module_dependencies.py 的 WEB_POM 指向 modules/applications/web/pom.xml，而实际模块是 shiyu-ai-web；own_implementation_artifact 还判断叶子名 implementation。实施时必须用真实反例确认其影响。
AuthServiceImpl 922 行且包含登录、注册、角色/租户切换、权限查询、Token 刷新、密码恢复；Agent contract POM 直接依赖 langgraph4j-core。

## 目标包结构与职责

```text
com.shiyu.ai.<domain>
├── contract                  # 仅 contract 模块
│   ├── api                   # 对外业务入口
│   ├── command               # 对外写入参数
│   ├── query                 # 对外查询参数
│   └── model                 # 稳定输出和值对象
└── implementation            # 仅 implementation 模块
    ├── application           # 用例编排、事务、assembler
    ├── domain                # 业务模型、规则、仓储端口
    ├── infrastructure        # persistence、外部 SDK、缓存适配
    └── web                   # Controller、request、response
```

领域为 iam、agent、conversation、education、governance、knowledge、memory、model、tooling。仅创建实际需要的子包。复杂领域在层内按 login、session、evaluation 等业务能力继续分组，不使用统一杂物 dto 包。

依赖规则：web -> application -> domain；infrastructure 实现 domain 端口；跨领域只依赖对方 contract。application 可调用本领域端口和其他领域 contract，不依赖具体 JDBC/SDK 类。contract 与 domain 不依赖 Spring Web、ORM 或具体推理框架。框架装配配置可置于 implementation.infrastructure.config，由组合根导入。

共享 kernel 只容纳跨领域稳定身份、上下文、错误和事件概念。common-core 的 HTTP、配置、事务、工具内容按实际依赖审查，不能为统一目录把一切迁入 kernel。
公共 web 模块只承担鉴权转换、过滤器、异常和通用配置；领域 Controller 留在领域实现的 web 包。组合根协调跨领域生命周期，不承载领域业务规则。

## 任务 1：生成归属清单并记录真实基线

文件：新增 scripts/architecture/inventory_java_packages.py、scripts/architecture/package-migration.csv、scripts/architecture/tests/test_package_inventory.py；读取所有 modules/**/pom.xml、生产源码和测试源码。

- [ ] 用 POM artifactId 建立模块清单；为每个生产文件记录 source_path、module、old_fqcn、target_fqcn、role、phase。内部嵌套类不当作独立文件迁移。
- [ ] 输出同名包、重复全限定类名、跨模块 import 和契约第三方依赖；通配/static import 不能默默忽略，无法归属者明确报错或进入人工核查列表。
- [ ] 建立临时目录测试：两个模块同包不同类应报告 split-package；相同 FQCN 应失败；测试和 target 目录不计入生产归属。
- [ ] 对实际仓库生成清单，人工逐项确认所有文件目标地址，检查目标 FQCN 唯一。清单在后续阶段同步更新，成为唯一迁移映射。
- [ ] 执行本计划的完整基线门禁，保存日志到 gitignored .testing/package-refactor/。失败记录原因并修正，不能以旧日志替代。
- [ ] 提交：test: inventory backend package ownership。

完成标准：每个生产源文件恰有一个归属；记录准确的 SHA、JDK 版本、命令和退出码。

## 任务 2：补齐真实模块边界检查

文件：修改 scripts/architecture/check_domain_module_dependencies.py、tests/shiyu-architecture-tests/src/test/java/com/shiyu/ai/architecture/ArchitectureRulesTest.java、.github/workflows/ci.yml；新增 scripts/architecture/tests/test_domain_boundaries.py。

- [ ] 在临时仓库夹具加入 A 实现依赖 B 实现、A 旧包 import B 旧包、contract import 实现三个反例，确认检查非零退出。
- [ ] 将旧叶子路径假设替换为根 POM 和 artifactId 识别；web POM 必须找到实际 shiyu-ai-web，缺失必须失败。
- [ ] 以任务 1 模块归属为依据检查所有生产源码；ArchUnit 在新包迁入后持续检查编译后的层间依赖。明确对通配、全限定引用与注解的检查方式。
- [ ] 为规则加入空匹配断言：契约和实现集合均非空，实际计数与清单一致。禁止 allowEmptyShould 掩盖迁移遗漏。
- [ ] 现有违规先形成逐类、逐依赖的有限迁移清单；不得放行整个领域包。迁移一项删除一项，禁止新增豁免。
- [ ] 执行反例测试和现有架构门禁，提交 test: enforce module ownership boundaries。

完成标准：三个反例均被拒绝；旧包也被检查；不存在路径缺失时静默通过。

## 任务 3：小领域试点并定型迁移操作

目录：modules/domains/conversation/shiyu-conversation-{contract,implementation}/src；涉及全仓消费者和测试。

- [ ] 按清单把 conversation.contract 类型归入 contract.api/model，把根包、chat、port、domain、web 和持久化分别映射到目标层。
- [ ] 用 git mv 移动文件，按完整 FQCN 更新 package/import；禁止对整个仓库做 conversation 字符串无差别替换。
- [ ] 同步测试包、扫描配置、注入引用及文档；枚举迁移前后 Bean 名称，类名不变但跨模块重名时显式处理冲突。
- [ ] 验证对话创建、消息保存、流式完成/失败/取消的现有测试，比较 HTTP/JSON 输出。
- [ ] 执行全仓 Maven 门禁，确认所有消费者可编译；检查源目录与 package 一致，旧 FQCN 仅允许出现在迁移清单历史列。
- [ ] 提交 refactor: normalize conversation packages。

完成标准：试点生产类全部归入目标结构、无重复类、无接口行为变化。

## 任务 4：其余常规领域分批迁移

按以下顺序执行，每行独立迁移、测试、审查和提交；不得跨批留下编译失败：

|批次|文件范围|重点与验证|
|---|---|---|
|4A|modules/domains/iam/shiyu-iam-*/src|auth -> iam.implementation；保持认证/角色/租户上下文和 MyBatis 映射行为；此批不拆服务|
|4B|modules/domains/knowledge/shiyu-knowledge-*/src|dto/retrieval/port 的公开类型进入 contract；索引实现进入 infrastructure；验证租户检索、文档删除、索引恢复|
|4C|modules/domains/memory/shiyu-memory-*/src|拆开 magma 同名包；公开记忆接口与内部算法分开；验证写入、读取、租户隔离|
|4D|modules/domains/model/shiyu-model-*/src|chat/embedding/port 公开接口与 SDK/gateway 适配分开；验证路由、流式错误、取消和超时，使用可控替身|
|4E|modules/domains/tooling/shiyu-tooling-*/src|tool/plugin 收敛到 tooling；插件 SPI 若为外部扩展点先记录二进制兼容策略；验证插件加载、签名和 worker 生命周期|
|4F|modules/domains/education/shiyu-education-*/src|request/vo -> web；业务输出和用例参数按消费者定位；验证教育业务 CRUD、租户与权限|
|4G|modules/domains/governance/shiyu-governance-*/src|usage/quota/application/web 收敛分层；验证额度、使用量和生命周期|

每批步骤：确认清单 -> 移动类型 -> 更新全仓消费者和资源字符串 -> 运行领域测试 -> 运行边界检查 -> 完整 Maven verify -> 提交。提交名为 refactor: normalize <领域> packages，其中领域取表中固定名称。
对外插件若依赖旧 Java SPI，采用有截止阶段的兼容适配或同步升级插件；在兼容验证完成前不删除旧 SPI。数据库中若持久化类名，使用旧数据副本验证读取与迁移，禁止直接改生产数据。

## 任务 5：Agent 契约去框架耦合

文件范围：modules/domains/agent/shiyu-agent-contract/pom.xml、两个 Agent 模块 src，以及所有 runtime/node 消费者。

- [ ] 从清单列出所有 langgraph4j 类型在契约 public/protected 签名、泛型、继承和注解中的出现点。
- [ ] 将仅实现使用的 NodeFields、node creator、执行图辅助类型移入 implementation；仅实际跨领域需要的类型保留 contract。
- [ ] 对外确需的运行输入/结果定义项目自有不可变类型；在 implementation.infrastructure 的框架适配器中转换。保持状态、错误、取消和恢复语义。
- [ ] 为 public API 加负向依赖测试：contract 引用 langgraph4j 时必须失败；contract POM 移除 langgraph4j 后仍能独立编译。
- [ ] runtime 包分别迁入 agent.contract 与 agent.implementation，更新调用方；用已有节点/图执行、审批、暂停恢复测试验证适配行为。
- [ ] 完整门禁通过后提交 refactor: isolate agent execution contracts。

完成标准：Agent 契约不再暴露或直接依赖具体图执行框架；全部八个历史 split-package 清零。

## 任务 6：非领域模块逐模块整治（必做）

本阶段覆盖全部 applications、infrastructure、shared 生产类及对应测试。每个模块必须提交逐类归属清单和验证结果；保留合理包结构也需记录依据，不能仅做抽样审查。公共模块不套用领域 contract/implementation 层级，按技术能力组织。

### 6A：shared-kernel 与 common-core 的基础边界

文件范围：modules/shared/shiyu-shared-kernel/src、modules/infrastructure/shiyu-common-core/src 及全仓消费者；更新各受影响 pom.xml。

- [ ] kernel 保留 context、event、error、page；逐项确认 UserId/TenantId/RoleId、ActorContext 是稳定值对象。检查 TenantScope 是否携带线程状态：技术上下文与纯身份模型分离，迁移时同步入口、持久化和线程模块消费者。
- [ ] 为 core 的每个类确定能力包：api、exception、validation、text、time、reflection、transaction、config；现有 utils 逐类按用途归位，禁止生成新的通用 helper 收容包。
- [ ] HTTP 专属响应/异常适配/XSS 请求处理迁入 common-web；文件 I/O 工具根据实际复用归入 storage 或保留独立通用工具，不让 core 反向依赖 storage。
- [ ] 比较 kernel.event.DomainEvent 与 core.tx.event.DomainEvent 的语义和消费者，明确前者业务事件、后者事务适配的关系；不因同名直接合并。
- [ ] 新增边界测试：kernel 禁止 Spring/ORM/Servlet；core 禁止依赖领域实现和 applications；故意加入违规依赖时必须失败。
- [ ] 验证身份值对象、分页、异常映射、事务事件提交/回滚行为；全量构建后提交 refactor: clarify kernel and core responsibilities。

### 6B：common-mybatis

文件范围：modules/infrastructure/shiyu-common-mybatis/src。

目标能力包：config、datasource、mapper、model、handler；core.mapper 收敛为 mapper，技术层不命名为业务 domain。

- [ ] 清点监听器、租户工厂、异常处理、数据源与基础模型；持久化模型不得成为跨领域公开业务契约。
- [ ] 核查租户和乐观锁配置中的领域特定逻辑；通用机制保留本模块，领域规则/实体特判移回所属领域适配层。
- [ ] 同步 MapperScan、XML namespace、typeAlias 和资源路径；验证数据源初始化、租户隔离、乐观锁冲突、异常转换。
- [ ] 边界检查通过后提交 refactor: organize mybatis support packages。

### 6C：common-web 与 applications/shiyu-ai-web

文件范围：两个模块 src；涉及 application 层消费者和 Spring 配置资源。

- [ ] common-web 保留可复用 auth/filter/interceptor，新增 exception 或 validation 包承接实际迁入能力；禁止引用领域具体 ServiceImpl。
- [ ] shiyu-ai-web 中 AuditInterceptor 归 interceptor；SaTokenExceptionHandler 和 ApiExceptionHandler 归 exception；XssProperties 按配置职责归 config.properties。
- [ ] SaTokenConfig、SaInterceptorConfig、ResourcesConfig、FilterConfig、OpenApiConfig 保持 config；SaPermissionProvider 保持 auth。检查 AgentWebMvcConfig/EduWebMvcConfig 是否含领域规则，规则留领域、通用 MVC 装配留入口。
- [ ] 为配置迁移验证 Bean 唯一性、过滤器顺序、鉴权拒绝、CORS、静态资源、异常 HTTP 状态与 OpenAPI 路由；禁止包移动导致重复注册。
- [ ] 提交 refactor: separate web configuration and adapters。

### 6D：common-storage

文件范围：modules/infrastructure/shiyu-common-storage/src。

- [ ] 保留 api/file/metadata/backup/config/lease/rate/security/vector/web 的能力分类，逐类确认准入；JdbcStorageMetadataStore 与 NoopStorageMetadataStore 归 metadata。
- [ ] 区分 ResumableUploadHandler 的传输职责与 ResumableUploadService 的上传协调：读取 Servlet/HTTP 对象者归 web，纯文件处理者留 file，并同步消费者。
- [ ] 核查 FileController 的权限和业务协调，技术上传端点可留 storage.web；领域规则通过契约调用，不能直接依赖领域实现。
- [ ] 明确 VectorIndexStore 与 common-vector 的边界：持久化索引文件与向量搜索分开；lease/rate 若是通用内存协调，记录实际消费者后归最合适现有模块，不强建单类模块。
- [ ] 验证本地/S3 替身、分片重试/合并、路径穿越、内容扫描、元数据存取和备份恢复；提交 refactor: enforce storage capability boundaries。

### 6E：common-thread

文件范围：modules/infrastructure/shiyu-common-thread/src。

- [ ] 保留 api/config/context/metrics；core 中 ExecutorFactory 接口与实现区分，公开扩展接口归 api，执行器实现归 executor，组合装饰器归 context。
- [ ] otel 和 metrics 是可选观测适配，确认 api 不反向引用 OpenTelemetry/Micrometer；避免单纯为了统一而增加重复包装。
- [ ] 核查 ThreadPoolConfig 与 ThreadingAutoConfiguration、两种 Properties 的绑定范围，只有功能重叠才合并，保持现有配置键兼容。
- [ ] 验证线程上下文传播及清理、异常任务、拒绝任务、线程池关闭、虚拟线程与平台线程策略；提交 refactor: organize thread execution support。

### 6F：common-vector

文件范围：modules/infrastructure/shiyu-common-vector/src 及 storage/knowledge/memory 消费者。

- [ ] 根包 com.shiyu.ai.vector 迁为 com.shiyu.ai.common.vector；接口归 api，VectorRecord/SearchRequest/Options/SearchType 归 model，具体存储归 implementation，工厂归 factory，自动装配归 config。
- [ ] 更新 Spring AutoConfiguration.imports、工厂绑定、反射字符串和测试；不改变索引格式、距离度量和默认参数。
- [ ] 验证内存与 JVector 后端的插入/检索/删除、关闭重开、维度错误和索引兼容；提交 refactor: normalize vector support packages。

### 6G：applications/shiyu-application 组合根

文件范围：modules/applications/shiyu-application/src 及 bootstrap 配置。

目标包：com.shiyu.ai.composition 下 integration/governance、retention、database；Maven 坐标保持 shiyu-application，文档明确其为组合根。

- [ ] UsageEventListener、QuotaGenerationAdmission、ConversationUsageSink 作为跨领域接线适配归 integration.governance；额度业务规则保留 governance 领域。
- [ ] DataRetentionService/Properties 归 retention，DatabaseInitializer 归 database；初始化器只编排模块初始化，领域 schema 定义仍归领域。
- [ ] 更新全仓 import、组件扫描和测试；验证事件只订阅一次、配额拒绝、保留策略、数据库首次初始化及重复启动。
- [ ] 检查领域及公共基础设施不得反向依赖 composition，提交 refactor: clarify composition root packages。

### 6H：applications/shiyu-ai-bootstrap

文件范围：modules/applications/shiyu-ai-bootstrap/src 和 pom.xml 打包配置。

- [ ] ShiyuBootstrapApplication 保持 bootstrap 根包；LogRetentionService/Properties 移至 bootstrap.retention，EmbeddedDataDirectoryLock 移至 bootstrap.lock。
- [ ] ApplicationStartupListener 归 bootstrap.lifecycle；config 仅放真正配置类，避免把监听器一律塞入 config。
- [ ] 保持 main-class 配置、APP_HOME 解析、锁归属、关闭钩子和日志清理行为；验证同目录重复启动拒绝、锁释放、全新 APP_HOME 启动。
- [ ] 提交 refactor: organize bootstrap lifecycle support。

### 6I：tests 与 scripts 的配套分类

文件范围：tests/shiyu-architecture-tests/src/test、scripts/architecture、scripts/docs；scripts/continuous_testing 仅做职责清点，保持停用。

- [ ] 架构测试按 module/package/layer/context 分类，拆分单一 ArchitectureRulesTest 的不同规则职责；保留测试发现和非空断言。
- [ ] 单元测试随生产包迁移；跨模块集成测试留独立测试模块，避免倒灌生产模块依赖。
- [ ] architecture 检查脚本区分清单生成与门禁执行，共享源码归属解析只有实际复用时抽取；文档脚本保留 docs 归属。
- [ ] 对 continuous_testing 记录状态、调度、执行、报告各组件边界及未完成功能，不把 Python 模块套为 Java 包，不启动服务。
- [ ] 运行规则反例、测试发现与文档检查，提交 test: organize architecture verification responsibilities。

阶段完成标准：3 个 applications、6 个 infrastructure、1 个 shared 模块全部有类归属清单、目标包、实际验证和剩余问题记录；禁止只完成 domains 就宣布全项目完成。

## 任务 7：拆分 AuthServiceImpl 用例职责

文件：迁移后的 iam/implementation/application 下建立 authentication、session、identity、recovery 能力；保留原入口作薄协调，直到所有调用方验证完成。

- [ ] 从原方法提取行为矩阵：密码登录与验证码登录；注册；刷新/登出；切换角色/租户；权限查询；找回密码。
- [ ] 补充行为测试：无权租户切换被拒绝且旧会话有效；失效 Token 不刷新；密码恢复失败不改密码；权限查询不串租户；事务失败不留下半成品。
- [ ] 先移动会话管理，再移动身份切换，再移动恢复流程，最后缩小认证协调；每次移动只改变委托位置。
- [ ] 抽出的类通过构造器注入明确仓储/端口；禁止所有子服务反向依赖旧 AuthServiceImpl。
- [ ] 特别验证 Spring 事务代理：从自调用改为跨 Bean 调用后传播/回滚与原要求一致；不将 ActorContext 改回线程隐式读取。
- [ ] 全部门禁通过后提交 refactor: separate iam authentication use cases。

完成标准：各类职责单一、依赖无环、原外部入口语义保持；不以行数作为唯一验收标准。

## 任务 8：Agent 和索引大类专项收敛

文件：任务 1 映射后的 AgentRuntimeImpl、NodeFactory、knowledge EmbeddedIndexRegistry 及其测试。

- [ ] AgentRuntimeImpl 区分运行协调、状态持久化、事件发布和框架适配，保留单一运行生命周期入口。
- [ ] NodeFactory 按实际节点注册/构造职责拆分，验证未知类型、重复注册、缺少依赖的错误语义，避免引入新的反射全局注册表。
- [ ] EmbeddedIndexRegistry 拆分索引生命周期与具体后端打开/恢复逻辑；测试关闭后操作、重复关闭、打开失败、恢复失败和并发取得实例。
- [ ] 每个类先建立现有行为保护，再做职责移动；不改算法和性能参数。若代码审查证明无需拆分，记录职责与依赖证据，保留实现。
- [ ] 独立验证后按领域提交，完整门禁作为最终收口。

## 任务 9：文档、兼容和最终验收

文件：docs/模块结构与命名.md、docs/系统全景与模块调用.md、docs/开发规范与对话决策.md、迁移清单、CI。

- [ ] 修正文档中“所有 Controller 放 applications”与实际领域模块规则的冲突，记录公共技术 Controller 的准入规则。
- [ ] 生成最终清点，要求生产文件 100% 有模块与层归属、无重复 FQCN、无跨契约实现同名包、无剩余迁移豁免。
- [ ] 用临时违规夹具再次验证 CI 能拒绝跨领域实现依赖、contract 框架依赖、domain 反向依赖、空扫描。
- [ ] 对迁移前后 OpenAPI 路由、schema、字段、权限和错误码做差异比较；对旧数据副本执行启动、登录、对话、知识检索、上传和插件加载回归。
- [ ] 清理构建产物后执行完整门禁，防止旧 .class 使包迁移假通过。
- [ ] 记录未执行项与原因；所有要求通过后才提交最终报告并进行 squash 集成。

## 验证命令

以下命令在后端根目录用 PowerShell 分别执行，逐项检查退出码；不要用最后一个命令的成功覆盖前面的失败。

```powershell
python -m unittest discover -s scripts/architecture/tests -p 'test_*.py'
mvn --batch-mode --no-transfer-progress -Pstrict-warnings clean verify "-Ddependency-check.skip=true"
python scripts/verify_fresh_startup.py
python scripts/architecture/check_domain_coverage.py
python scripts/docs/verify_documentation.py
python scripts/architecture/check_legacy_routes.py
python scripts/architecture/check_backend_terminology.py
python scripts/architecture/check_domain_module_dependencies.py
python scripts/architecture/check_schema_ownership.py
python scripts/architecture/check_backend_module_names.py
```

首条在任务 1 创建目录和测试后执行。dependency-check 明确跳过，因此结果不包含漏洞扫描结论。每批可以先做受影响模块测试，但跨域类路径迁移必须通过全仓编译与测试。
前端在 shiyu-ui 执行 pnpm test:unit；真实联调沿用现有 E2E 入口并记录运行命令，未运行不能算通过。包迁移不需要调用收费真实模型供应商。

## 集成、回滚和验收矩阵

实施开始记录 master SHA，并在隔离分支工作。每批保持可构建；失败留在该批修复，不推进后续批次。回滚必须把移动类、消费者、配置与测试一起 revert，不能只移回文件。涉及持久化类名时同时保留数据备份和兼容读取方案。

|要求|证明材料|
|---|---|
|类归属完整|inventory 清单与源文件集合逐项相等|
|边界可验证|违规夹具失败、合法夹具通过、CI 实际执行|
|契约独立|contract 编译通过、框架类型与实现依赖检查无违规|
|行为兼容|HTTP/OpenAPI 差异、业务回归、旧数据与插件兼容结果|
|构建无残留|clean verify、fresh APP_HOME 启动通过|
|大类职责收敛|方法/依赖分工表、行为测试、无反向循环|
|文档一致|命名规则与实际清单一致、文档检查通过|
|历史简洁|最终 squash 提交与已验证分支 tree hash 一致|

执行顺序固定为 1 -> 2 -> 3 -> 4A–4G -> 5 -> 6A–6I -> 7 -> 8 -> 9。最大风险是 Java SPI/持久化类名兼容、框架类型泄漏和事务代理变化；这些必须有专门验证，不能用目录移动成功替代。
