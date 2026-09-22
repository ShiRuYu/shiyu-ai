# 单一应用与业务模块开关实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将仓库收敛为一个可执行的 Spring Boot 应用，并让教育、未来新增业务模块通过统一模块装配协议和开关启停，而不是增加新的 `application` 启动模块。

**Architecture:** 平台能力始终由单一启动类装配；每个可选业务模块提供自己的自动配置、模块描述和数据库贡献者。进程级开关决定模块 Bean、路由、节点和初始化资源是否装配，租户级能力由 IAM 授权决定，二者不混用。应用不再扫描整个 `com.shiyu.ai`，只扫描组合根，业务实现通过 Spring Boot 自动配置显式加入。

**Tech Stack:** Java 21、Spring Boot 4.1、Maven、MyBatis-Flex、Spring MVC、Sa-Token、JUnit 5、ArchUnit、H2。

**Spec:** 本文档由当前架构盘点和“长期方案，不要多个 application”的决策生成。

## Global Constraints

- 最终只保留 `modules/applications/shiyu-ai-bootstrap` 一个可执行应用模块和一个 `ShiyuBootstrapApplication` 启动类。
- 删除 `shiyu-platform-bootstrap`，不保留第二个可执行 Jar、第二个端口入口或第二套默认配置。
- 模块开关统一使用 `shiyu.modules.<module-id>.enabled`；环境变量使用 `SHIYU_MODULE_<MODULE_ID>_ENABLED`。每个模块必须显式声明缺省值，条件类不得自行决定所有模块的默认状态。
- 全局开关关闭时不得注册该模块的 Bean、Controller、公开路径、Agent 节点或数据库基线贡献者；不得删除已经存在的数据表。
- 租户功能开通使用 IAM 契约和当前 `ActorContext` / `TenantScope`，不得通过修改 Spring 全局属性实现租户级开关。
- 保持已有 HTTP URL、请求方法、权限码、Bean 名称、租户隔离和数据库行为；模块关闭时新增的结果只允许是“路由不存在”或明确的模块未启用错误。
- 每个新增模块必须有自动配置、模块开关测试、禁用状态测试和文档，不得重新使用根包隐式扫描。

---

### Task 1: 建立通用业务模块契约

**Files:**
- Create: `modules/infrastructure/shiyu-common-foundation/src/main/java/com/shiyu/ai/common/foundation/module/BusinessModuleDescriptor.java`
- Create: `modules/infrastructure/shiyu-common-foundation/src/main/java/com/shiyu/ai/common/foundation/module/ConditionalOnBusinessModule.java`
- Create: `modules/infrastructure/shiyu-common-foundation/src/main/java/com/shiyu/ai/common/foundation/module/BusinessModuleCondition.java`
- Test: `modules/infrastructure/shiyu-common-foundation/src/test/java/com/shiyu/ai/common/foundation/module/BusinessModuleConditionTest.java`

**Interfaces:**
- `BusinessModuleDescriptor` 提供 `id()`、`displayName()`、`routePrefixes()` 和 `permissionPrefix()`，只表达模块元数据，不依赖任何业务实现。
- `@ConditionalOnBusinessModule(value = "education", matchIfMissing = true)` 读取 `shiyu.modules.education.enabled`；`matchIfMissing` 由模块自动配置显式决定。
- `BusinessModuleCondition` 使用 Spring `Environment` 读取属性，并支持 `SHIYU_MODULE_EDUCATION_ENABLED` 环境变量映射。

- [ ] **Step 1: Write the failing test**

测试覆盖：`matchIfMissing=true` 和 `false` 的缺省行为、显式 `true` 时启用、显式 `false` 时禁用、环境变量覆盖、非法值按启动失败处理，以及模块 ID 只能包含小写字母、数字和短横线。

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-foundation -Dtest=BusinessModuleConditionTest test -Ddependency-check.skip=true`

Expected: FAIL because the annotation, condition and descriptor do not exist.

- [ ] **Step 3: Write minimal implementation**

条件实现只负责进程级 Bean 装配；不要在条件类中访问数据库、读取租户或调用 IAM。模块条件直接读取 `shiyu.modules.<module-id>.enabled` 及其环境变量映射；不再引入无消费者的 `BusinessModuleProperties` 绑定类。教育模块的 IDE 提示由 `additional-spring-configuration-metadata.json` 提供。

- [ ] **Step 4: Run test to verify it passes**

Run the same command and expect all module-condition tests to pass.

- [ ] **Step 5: Commit**

```bash
git add modules/infrastructure/shiyu-common-foundation/src/main/java/com/shiyu/ai/common/foundation/module modules/infrastructure/shiyu-common-foundation/src/test/java/com/shiyu/ai/common/foundation/module
git commit -m "feat: add generic business module condition"
```

### Task 2: 收敛为单一启动应用

**Files:**
- Modify: `pom.xml`
- Modify: `modules/applications/shiyu-ai-bootstrap/pom.xml`
- Modify: `modules/applications/shiyu-ai-bootstrap/src/main/java/com/shiyu/ai/bootstrap/ShiyuBootstrapApplication.java`
- Modify: `modules/applications/shiyu-ai-bootstrap/src/main/resources/application.yml`
- Delete: `modules/applications/shiyu-platform-bootstrap/pom.xml`
- Delete: `modules/applications/shiyu-platform-bootstrap/src/main/java/com/shiyu/ai/platform/bootstrap/PlatformBootstrapApplication.java`
- Delete: `modules/applications/shiyu-platform-bootstrap/src/main/resources/application.yml`
- Delete: `modules/applications/shiyu-platform-bootstrap/src/test/java/com/shiyu/ai/platform/bootstrap/PlatformBootstrapDefaultsTest.java`
- Test: `modules/applications/shiyu-ai-bootstrap/src/test/java/com/shiyu/ai/bootstrap/SingleApplicationCompositionTest.java`

**Interfaces:**
- `ShiyuBootstrapApplication` 是唯一 `main` 入口，保留现有 `APP_HOME` 锁和关闭钩子。
- 根 `pom.xml` 不再包含 `shiyu-platform-bootstrap` 模块。
- 完整应用继续直接依赖 `shiyu-education-implementation`，但教育是否装配由 Task 4 的模块自动配置决定。

- [ ] **Step 1: Write the failing test**

测试检查 Maven reactor 中只有一个可执行 bootstrap，启动类的组件扫描只覆盖 `com.shiyu.ai.bootstrap` 和 `com.shiyu.ai.composition`，不再使用 `scanBasePackages = "com.shiyu.ai"`。

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-bootstrap -am -Dtest=SingleApplicationCompositionTest test -Ddependency-check.skip=true`

Expected: FAIL because platform bootstrap still存在，且启动类仍进行根包扫描。

- [ ] **Step 3: Write minimal implementation**

将平台默认配置合并到完整应用的 `application.yml`，增加：

```yaml
shiyu:
  modules:
    education:
      enabled: ${SHIYU_MODULE_EDUCATION_ENABLED:true}
```

删除平台启动模块及根 POM 引用；不要删除教育实现模块本身。

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-bootstrap -am test -Ddependency-check.skip=true`

Expected: 单应用组合测试和已有启动测试通过。

- [ ] **Step 5: Commit**

```bash
git add pom.xml modules/applications/shiyu-ai-bootstrap modules/applications/shiyu-platform-bootstrap
git commit -m "refactor: use one application bootstrap"
```

### Task 3: 让平台实现通过显式组合加载

**Files:**
- Create: `modules/applications/shiyu-application/src/main/java/com/shiyu/ai/composition/config/PlatformCompositionAutoConfiguration.java`
- Create: `modules/applications/shiyu-application/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Modify: `modules/applications/shiyu-application/pom.xml`
- Modify: `modules/applications/shiyu-ai-web/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Test: `tests/shiyu-architecture-tests/src/test/java/com/shiyu/ai/architecture/CompositionScanBoundaryTest.java`

**Interfaces:**
- `PlatformCompositionAutoConfiguration` 只扫描平台实现和组合根：`com.shiyu.ai.composition`、`com.shiyu.ai.iam.implementation`、`com.shiyu.ai.agent.implementation`、`com.shiyu.ai.conversation.implementation`、`com.shiyu.ai.model.implementation`、`com.shiyu.ai.memory.implementation`、`com.shiyu.ai.knowledge.implementation`、`com.shiyu.ai.tooling.implementation`、`com.shiyu.ai.governance.implementation`、`com.shiyu.ai.common.storage` 和 `com.shiyu.ai.web`。
- 业务模块包不得出现在平台扫描列表；业务模块必须通过自己的自动配置加入。
- 现有公共基础设施自动配置继续通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 加载。

- [ ] **Step 1: Write the failing test**

ArchUnit 测试读取启动类和组合配置，断言不存在 `com.shiyu.ai` 根包扫描，平台扫描列表不包含 `com.shiyu.ai.education.implementation`，并断言所有可选业务实现均有自动配置入口。

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn --batch-mode --no-transfer-progress -pl tests/shiyu-architecture-tests -am -Dtest=CompositionScanBoundaryTest test -Ddependency-check.skip=true`

Expected: FAIL because当前启动类仍扫描整个根包且教育没有自动配置入口。

- [ ] **Step 3: Write minimal implementation**

平台组合配置使用显式包列表，不使用 `com.shiyu.ai` 通配扫描。保留现有公共自动配置文件，不把业务类复制到应用模块。

- [ ] **Step 4: Run test to verify it passes**

Run the same architecture-test command and then start the application with a fresh `APP_HOME`; expect IAM、Agent、Knowledge、Governance、Web 等平台接口仍能加载。

- [ ] **Step 5: Commit**

```bash
git add modules/applications/shiyu-application modules/applications/shiyu-ai-web tests/shiyu-architecture-tests
git commit -m "refactor: make platform composition explicit"
```

### Task 4: 将教育改造成第一个可选业务模块

**Files:**
- Create: `modules/business/education/shiyu-education-implementation/src/main/java/com/shiyu/ai/education/implementation/config/EducationModuleAutoConfiguration.java`
- Create: `modules/business/education/shiyu-education-implementation/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Modify: `modules/business/education/shiyu-education-implementation/pom.xml`
- Test: `modules/business/education/shiyu-education-implementation/src/test/java/com/shiyu/ai/education/implementation/config/EducationModuleAutoConfigurationTest.java`
- Test: `modules/applications/shiyu-ai-bootstrap/src/test/java/com/shiyu/ai/bootstrap/BusinessModuleContextTest.java`

**Interfaces:**
- `EducationModuleAutoConfiguration` 使用 `@AutoConfiguration`、`@ConditionalOnBusinessModule(value = "education", matchIfMissing = true)` 和 `@ComponentScan("com.shiyu.ai.education.implementation")`。
- 自动配置提供 `BusinessModuleDescriptor("education", "教育", ["/api/education"], "edu:")`。
- `EducationDatabaseBaselineContributor`、教育 Mapper、Service、Controller、Agent 节点和公开路径都只能从该自动配置进入上下文。

- [ ] **Step 1: Write the failing test**

测试以两个 Spring `ApplicationContextRunner` 场景验证：

```java
contextRunner.withPropertyValues("shiyu.modules.education.enabled=false")
    .run(context -> assertThat(context).doesNotHaveBean(EducationDatabaseBaselineContributor.class));
contextRunner.withPropertyValues("shiyu.modules.education.enabled=true")
    .run(context -> assertThat(context).hasSingleBean(EducationDatabaseBaselineContributor.class));
```

同时验证禁用时 `/api/education/**` 不注册，启用时教育 Controller、Mapper 和节点创建器存在。

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn --batch-mode --no-transfer-progress -pl modules/business/education/shiyu-education-implementation -am -Dtest=EducationModuleAutoConfigurationTest,BusinessModuleContextTest test -Ddependency-check.skip=true`

Expected: FAIL because教育组件仍可能被根包扫描且没有条件自动配置。

- [ ] **Step 3: Write minimal implementation**

将教育组件的扫描入口集中到 `EducationModuleAutoConfiguration`，不要给 50 多个教育类逐一添加条件注解。禁用模块不删除表、不执行清理 SQL、不注销权限数据。

- [ ] **Step 4: Run test to verify it passes**

Run the same command and expect both enabled/disabled context tests to pass.

- [ ] **Step 5: Commit**

```bash
git add modules/business/education/shiyu-education-implementation
git commit -m "feat: make education a conditional business module"
```

### Task 5: 固化未来业务模块接入协议

**Files:**
- Create: `docs/architecture/business-module-convention.md`
- Modify: `docs/开发规范与对话决策.md`
- Test: `tests/shiyu-architecture-tests/src/test/java/com/shiyu/ai/architecture/BusinessModuleConventionTest.java`

**Interfaces:**
- 新业务实现必须提供 `{Module}ModuleAutoConfiguration`、自动配置 imports 文件、`BusinessModuleDescriptor` Bean 和独立模块测试。
- 新业务包不得被 `PlatformCompositionAutoConfiguration` 扫描；不得在应用启动类增加业务包名。
- 数据库使用现有 `DatabaseBaselineContributor`，Web 公开路径使用现有 `WebPublicPathContributor`；两个贡献者由模块自动配置条件控制。

- [ ] **Step 1: Write the failing test**

架构测试扫描 `modules/business/*/*-implementation`，要求每个业务实现有自动配置类、imports 文件和模块开关测试；同时禁止启动类和平台组合配置引用具体业务实现类。

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn --batch-mode --no-transfer-progress -pl tests/shiyu-architecture-tests -am -Dtest=BusinessModuleConventionTest test -Ddependency-check.skip=true`

Expected: FAIL until教育模块和约束扫描器完成登记。

- [ ] **Step 3: Write minimal implementation**

在开发规范中加入固定模板：模块 ID、默认值、自动配置入口、路由前缀、权限前缀、Schema/Seed 贡献者、租户级授权、禁用行为和测试命令。扫描器只校验结构，不宣称业务隔离正确。

- [ ] **Step 4: Run test to verify it passes**

Run the architecture test and `python scripts/docs/verify_documentation.py`.

- [ ] **Step 5: Commit**

```bash
git add docs/architecture/business-module-convention.md docs/开发规范与对话决策.md tests/shiyu-architecture-tests
git commit -m "docs: define extensible business module convention"
```

### Task 6: 增加租户级模块授权，不把全局开关当作租户开关

**Files:**
- Create: `modules/domains/iam/shiyu-iam-contract/src/main/java/com/shiyu/ai/iam/contract/module/TenantModuleAccessPort.java`
- Create: `modules/domains/iam/shiyu-iam-implementation/src/main/java/com/shiyu/ai/iam/implementation/application/module/TenantModuleAccessService.java`
- Create: `modules/domains/iam/shiyu-iam-implementation/src/main/java/com/shiyu/ai/iam/implementation/port/repository/TenantModuleAccessRepository.java`
- Create: `modules/domains/iam/shiyu-iam-implementation/src/main/java/com/shiyu/ai/iam/implementation/persistence/mapper/TenantModuleAccessMapper.java`
- Create: `modules/domains/iam/shiyu-iam-implementation/src/main/java/com/shiyu/ai/iam/implementation/persistence/repository/TenantModuleAccessRepositoryImpl.java`
- Create: `modules/applications/shiyu-ai-web/src/main/java/com/shiyu/ai/web/interceptor/BusinessModuleAccessInterceptor.java`
- Create: `modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/updates/iam/20260916_tenant_module_access.sql`
- Test: `modules/domains/iam/shiyu-iam-implementation/src/test/java/com/shiyu/ai/iam/implementation/application/module/TenantModuleAccessServiceTest.java`
- Test: `modules/applications/shiyu-ai-web/src/test/java/com/shiyu/ai/web/module/ModuleAccessInterceptorTest.java`

**Interfaces:**
- `TenantModuleAccessPort#isEnabled(TenantId tenantId, String moduleId)` 是业务模块访问 IAM 的唯一契约。
- IAM 保存 `AUTH_TENANT_MODULE(TENANT_ID, MODULE_ID, STATUS, CREATE_TIME, UPDATE_TIME)`，关闭模块只改变状态，不删除业务表和历史数据。
- `BusinessModuleAccessInterceptor` 根据已启用的 `BusinessModuleDescriptor.routePrefixes()` 做服务端检查；用户仍须通过已有 `SaCheckPermission` 权限校验。`WebPublicPathContributor` 继续只描述认证豁免路径，不承担模块授权。
- 后台任务和 Agent 执行入口调用同一端口，不依赖 HTTP 拦截器。

- [ ] **Step 1: Write the failing test**

覆盖当前租户启用、当前租户关闭、切换租户后重新判断、缺少权限和归属租户不一致五种情况；关闭时返回权限错误，不被通用异常处理改写为成功响应。

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn --batch-mode --no-transfer-progress -pl modules/domains/iam/shiyu-iam-implementation,modules/applications/shiyu-ai-web -am -Dtest=TenantModuleAccessServiceTest,ModuleAccessInterceptorTest test -Ddependency-check.skip=true`

Expected: FAIL because当前系统没有通用租户模块授权表和入口。

- [ ] **Step 3: Write minimal implementation**

模块访问判断顺序固定为：进程级模块启用 → 当前租户模块启用 → 当前用户权限 → 领域对象权限。平台管理员不能因为角色名称自动绕过模块开关。

- [ ] **Step 4: Run test to verify it passes**

Run the same command and then run the existing tenant switching and isolation test suite.

- [ ] **Step 5: Commit**

```bash
git add modules/domains/iam modules/applications/shiyu-ai-web modules/infrastructure/shiyu-common-web
git commit -m "feat: enforce tenant business module access"
```

### Task 7: 完成单应用验证和迁移清理

**Files:**
- Modify: `docs/使用文档.md`
- Modify: `docs/部署运维手册.md`
- Modify: `docs/architecture/education-boundary.md`
- Modify: `docs/architecture/branch-and-module-inventory.md`
- Test: `scripts/docs/verify_documentation.py`
- Test: `tests/shiyu-architecture-tests`

- [ ] **Step 1: 验证启动矩阵**

执行：

```text
mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-bootstrap -am test -Ddependency-check.skip=true
$env:SHIYU_MODULE_EDUCATION_ENABLED = 'false'
java -jar shiyu-ai-bootstrap.jar
$env:SHIYU_MODULE_EDUCATION_ENABLED = 'true'
java -jar shiyu-ai-bootstrap.jar
Remove-Item Env:SHIYU_MODULE_EDUCATION_ENABLED
```

验证两个开关状态使用同一个 Jar、同一个启动类和同一个端口配置；禁用状态不注册教育路由和教育基线，启用状态保持 `/api/education/**` 不变。

- [ ] **Step 2: 验证全仓库引用**

执行：

```text
rg -n "PlatformBootstrapApplication|shiyu-platform-bootstrap|platform-bootstrap" . --glob '!**/target/**'
python scripts/docs/verify_documentation.py
python scripts/architecture/check_domain_module_dependencies.py
git diff --check
```

预期除了迁移说明中的历史记录外，不存在第二个启动类、第二个应用模块或旧启动命令。

- [ ] **Step 3: 执行完整验收**

```text
mvn --batch-mode --no-transfer-progress test -Ddependency-check.skip=true
mvn --batch-mode --no-transfer-progress -Pstrict-warnings compile -DskipTests=true -Ddependency-check.skip=true
```

明确记录 Docker 不可用时 Kafka、PgVector、Minio 和 Redis 测试的跳过状态，不将跳过表述为通过。

- [ ] **Step 4: 提交最终迁移**

```bash
git add pom.xml modules docs scripts tests
git commit -m "refactor: consolidate runtime into one configurable application"
```

## Decision Summary

- 最终不保留 `platform-bootstrap`；平台版不再通过第二个应用表达，而是通过 `shiyu.modules.<module>.enabled` 和租户授权表达。
- Education 只是第一个迁移样本，后续课程、企业、工作流、插件扩展等业务模块必须复用同一自动配置和模块协议。
- 全局 Spring 开关解决“当前进程是否装配模块”；IAM 租户开关解决“当前租户是否购买/启用模块”；权限码解决“当前用户是否可以操作模块”。三者不能由一个布尔值替代。
- 关闭模块不会删除数据库表和历史数据；重新开启时复用原有数据并重新注册 Bean、路由和节点。
