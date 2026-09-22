# Spring Boot 4.2.0-M1 与依赖升级实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将项目升级到 Spring Boot 4.2.0-M1，验证 Spring Framework 7.1 的 HTTP QUERY 支持，仅在实际可编译并适合业务语义的接口上迁移，并完成有证据的稳定依赖升级。

**Architecture:** 保持现有模块边界、权限、租户、数据库和响应结构不变。QUERY 只用于复杂查询请求体场景；简单分页和筛选继续保留 GET。依赖升级优先采用稳定版本，拒绝把里程碑、候选版误当成稳定版。

**Tech Stack:** Java 21、Maven、Spring Boot 4.2.0-M1、Spring Framework 7.1.0-M1、Spring MVC、Springdoc、Sa-Token、MyBatis-Flex、JUnit 6。

**Spec:** 用户目标：升级 Spring Boot 4.2.0-M1；若框架支持且接口有必要则迁移 QUERY，否则保持原接口；审计并升级必要依赖；验证后提交并推送远端主分支。

## Global Constraints

- 不修改租户隔离、权限语义、数据库结构和已有响应模型。
- 不把所有 GET 列表接口机械替换为 QUERY；只有复杂查询体才允许迁移。
- 依赖升级必须记录当前版本、目标版本、稳定性和验证结果。
- 不覆盖工作区已有用户修改；提交前审查全部暂存、未暂存和未跟踪文件。

---

### Task 1: 升级 Spring Boot 并固定 Framework 版本

**Files:**
- Modify: `pom.xml`
- Test: Maven effective dependency tree and `RequestMethod` bytecode inspection

- [x] 将 `spring-boot.version` 改为 `4.2.0-M1`。
- [x] 运行 Maven 依赖树，确认 Spring Framework 统一解析为 `7.1.0-M1`。
- [x] 检查 `RequestMethod`、`HttpMethod` 和 MVC 映射 API；实际解析的 7.1.0-M1 不包含 `QUERY` 常量，因此不迁移路由。
- [x] 运行受影响模块编译并修复升级兼容性问题（包括 JVector 4.0.1 API 调整）。

### Task 2: 以测试先行验证 QUERY 路由能力

**Files:**
- Modify: `modules/applications/shiyu-ai-web/src/test/...` 或 Bootstrap 路由测试
- Modify: 需要迁移的 Controller（仅在 Task 1 通过后）

- [x] 通过本地 Spring Web 7.1.0-M1 字节码和 Spring MVC 集成依赖核验 QUERY 能力；当前版本不存在该 API，未添加不可编译的测试或映射。
- [x] 根据核验结果保持所有现有 GET/POST 路由；未发现必须迁移为 QUERY 的复杂查询体接口。
- [x] 确认 OpenAPI、路由完整性测试、前端客户端和权限测试无需因 QUERY 迁移变更。
- [x] 运行受影响模块测试和 Bootstrap 集成测试。

### Task 3: 依赖版本审计与必要升级

**Files:**
- Modify: 根 `pom.xml` 及实际声明版本的模块 POM
- Create/Modify: 依赖审计文档（如已有依赖清单文档）

- [x] 使用 `versions:display-dependency-updates` 和完整 dependency tree 生成清单。
- [x] 排除预发布、跨主版本和未经 API 验证的升级。
- [x] 升级有明确收益且可验证的稳定补丁版本，并记录保留版本与原因。
- [x] 每个升级批次先运行模块测试，再运行全量测试。

### Task 4: 全量验证、提交和推送

**Files:**
- All current tracked and untracked project changes after review

- [x] 运行架构脚本、文档检查、模块测试、Bootstrap 测试、严格编译和差异格式检查。
- [x] 检查工作区临时文件、构建产物和 dependency reports，不将其提交。
- [x] 检查远端主分支状态；`origin/HEAD` 指向 `origin/master`，远端无分叉，未使用强制推送。
- [x] 创建普通提交并推送到 `origin/master`，本次升级提交为 `d21681595909aa79536d2e4b1467f9dfce698607`，推送后已核对本地与远端提交号一致。

## 执行结果

- Spring Boot 已升级到 `4.2.0-M1`，实际解析 Spring Framework `7.1.0-M1`。
- 本地依赖字节码核验表明该 Spring Web 版本没有 `RequestMethod.QUERY` 或 `HttpMethod.QUERY`，因此没有把现有接口改成不可编译的 QUERY 路由。
- 已完成受影响模块测试、Bootstrap 集成测试、严格编译、架构脚本、中文/接口/功能注释扫描、文档检查和差异检查。
- 当前远端主分支为 `origin/master`；`origin/main` 不存在，因此没有创建第二个主分支。
