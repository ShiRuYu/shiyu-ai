# 错误记录与中文注释门禁实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use `executing-plans` to implement this plan task by task, with verification after each task.

**Goal:** 记录 OpenNLP 清单路径错误，建立依赖清单预检和中文注释门禁，并修复所有生产接口类型及关键类型的中文说明。
**Architecture:** 以 `scripts/architecture` 中的标准库检查器作为独立质量门禁；JavaDoc 负责接口契约和关键类型说明；CI 在 Maven 验证前执行预检，避免编译器晚期失败。
**Tech Stack:** Java 21、Maven、JUnit、Python 3 标准库、GitHub Actions；前端只扫描项目自有文件，不改变运行时依赖。
**Spec:** `docs/superpowers/specs/2026-09-11-error-recording-and-chinese-comments-design.md`。

## 全局约束

- 保留当前教育模块边界迁移和所有已有未提交改动，不使用回退或覆盖命令。
- 不改变 HTTP、JSON、数据库、权限、事务和业务行为。
- 注释、脚本输出、报告和新增文档使用中文；技术标识、命令和协议字段保持原样。
- 扫描排除构建产物、第三方代码、生成文件和历史备份。
- 每项任务完成后运行对应的最小验证；最终再运行全量门禁和严格 Maven 构建。

---

## 任务 1：记录已发生的构建错误并翻译配置说明

**文件：** 新增 `docs/工程错误记录.md`；修改 `pom.xml` 中与 `-Xlint:-path` 相关的注释。

- 记录症状、触发条件、根因、复现命令、修复命令、验证结果和后续排查顺序。
- 明确 `opennlp-tools-2.5.9.jar` 的清单目标缺失，以及 `-Werror` 放大诊断的关系。
- 使用中文说明，不翻译 Maven 坐标和命令参数。
- 验证 Markdown 链接、命令和 `git diff --check`。

## 任务 2：实现依赖清单预检及测试

**文件：** 新增 `scripts/architecture/check_dependency_manifests.py`、`scripts/architecture/tests/test_dependency_manifests.py`；修改 `.github/workflows/ci.yml`。

- 先写临时 JAR 夹具测试：合法相对路径通过、缺失目标失败、多行 `Class-Path` 正确解析、无清单通过。
- 实现命令行参数、中文错误输出、退出码和 Maven 坐标路径解析；禁止扫描无关的整个本地仓库。
- CI 先离线解析 agent implementation 及其上游依赖，再检查实际的 `opennlp-tools-2.5.9.jar`。
- 运行该脚本和 Python 测试，确认错误信息可定位到 JAR 和缺失文件。

## 任务 3：实现中文接口和注释扫描器

**文件：** 新增 `scripts/architecture/check_chinese_comments.py`、`scripts/architecture/tests/test_chinese_comments.py`；新增 `docs/工程注释扫描报告.md`；修改 CI。

- 识别所有生产接口声明并要求紧邻的类型级 JavaDoc包含中文字符。
- 抽取 Java、Python、TypeScript、YAML、XML、Markdown 和脚本注释，过滤代码块、技术标识、URL、标签和配置键。
- 对自然语言英文给出文件和行号；白名单必须有中文理由，不能用宽泛正则绕过。
- 测试排除目录、中文注释、纯技术标识和真实英文句子。

## 任务 4：补齐全部接口类型的中文 JavaDoc

**文件：** `modules/**/src/main/java/**/*.java`、`tests/**/src/test/java/**/*.java` 中的接口声明。

- 根据接口所在层次补充职责、边界和关键返回值说明；持久化 Mapper、仓储端口、SPI、框架回调也必须有类型级说明。
- 不修改方法签名、注解、泛型、导入和运行逻辑。
- 用扫描器反复检查接口缺失清单，直到生产接口为零；测试接口按同一规则处理或明确排除并记录。

## 任务 5：翻译自然语言英文并补充关键类型注释

**文件：** 后端、前端项目自有源文件和配置中扫描器报告的注释位置；重点覆盖公共 DTO、枚举、控制器、应用服务、领域类型和关键配置。

- 按扫描器输出逐项将自然语言注释改为中文，保留技术标识、代码、路径和协议文本。
- 在公共类型和关键入口补充简洁中文 JavaDoc 或块注释，避免为私有实现生成无意义文本。
- 每批修改后运行扫描器，防止英文遗漏和误改配置。

## 任务 6：生成报告并完成全量验证

**文件：** 更新 `docs/工程注释扫描报告.md`，必要时更新架构文档。

- 记录扫描时间、文件范围、接口数量、中文覆盖率、英文命中数、白名单和排除项。
- 运行 Python 架构测试、依赖预检、注释门禁、`git diff --check`。
- 运行 `mvn --batch-mode --no-transfer-progress -Pstrict-warnings clean verify -Ddependency-check.skip=true -Dmaven.compiler.useIncrementalCompilation=false`。
- 只有所有命令成功且差异审查无业务行为变化时才可宣称完成；不自动提交或推送。
