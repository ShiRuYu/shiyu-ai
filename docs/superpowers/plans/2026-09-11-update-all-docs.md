# 全部文档更新实施计划

> **For agentic workers:** Execute the checklist in this plan in the current worktree and run the verification commands before reporting completion.

**Goal:** 使仓库内项目说明、技术说明、部署运维、模块 README 与参考文档准确反映当前单体架构和可配置外部基础设施。

**Architecture:** 以源码、配置、Maven reactor、OpenAPI 和数据库脚本为事实源。先更新导航与总览，再同步运行配置、模块职责、部署迁移和质量报告，最后运行文档门禁检查本地链接、接口、表和权限统计。

**Tech Stack:** Markdown、Spring Boot YAML、Maven、Python documentation verifier。

**Spec:** 当前线程目标“更新全部文档”，以及《外部基础设施切换》中的 provider、迁移和回滚约束。

## Global Constraints

- 保持单体应用，不新增服务进程。
- 不修改 API、领域契约、包名和 Java 类路径。
- 外部地址、账号和密钥只通过环境变量或密钥管理系统提供。
- H2、本地文件、JVector、进程内事件和 disabled Redis 仍为默认本地方案。
- 文档中的接口、表和权限数量必须由当前生成器与门禁输出确认。

---

### Task 1: 盘点文档事实源

**Files:**
- Read: `README.md`, `README.en.md`, `docs/*.md`, module `README.md` files
- Read: `modules/applications/shiyu-ai-bootstrap/src/main/resources/application*.yml`
- Read: `scripts/docs/verify_documentation.py`

- [x] 对照当前 provider 配置、模块清单、数据库脚本和 OpenAPI 统计，记录需要修正的过时描述。

### Task 2: 更新总览与导航文档

**Files:**
- Modify: `README.md`
- Modify: `README.en.md`
- Modify: `docs/项目介绍.md`
- Modify: `docs/技术文档.md`
- Modify: `docs/系统全景与模块调用.md`
- Modify: `docs/模块结构与命名.md`
- Modify: `docs/文档导航.md`

- [x] 将可切换数据库、文件、向量、Redis、事件 provider 和默认值写入总览。
- [x] 保持模块、API 前缀、单体边界和类路径描述与源码一致。

### Task 3: 更新使用、部署和维护文档

**Files:**
- Modify: `docs/使用文档.md`
- Modify: `docs/部署运维手册.md`
- Modify: `docs/外部基础设施切换.md`
- Modify: `docs/开发规范与对话决策.md`
- Modify: `docs/安全与租户隔离审计.md`
- Modify: `docs/文档交付清单.md`

- [x] 补充 env-only 凭据、profile/provider 组合、数据库基线、文件迁移、向量重建、Redis 快速失败和事件 outbox/DLQ 操作说明。
- [x] 明确外部设施切换前的数据迁移、校验和回滚要求。

### Task 4: 同步模块 README 与质量报告

**Files:**
- Modify: all tracked module `README.md` files under `modules/`, `shared/`, and `tests/`
- Modify: `docs/质量与联调报告.md`
- Modify: `scripts/database/README.md`

- [x] 统一模块职责、依赖边界、启动配置和测试说明。
- [x] 更新验证日期、命令、统计和 Docker Testcontainers 的可执行条件。

### Task 5: 验证文档并提交

- [x] Run `python scripts/docs/verify_documentation.py` and require exit code 0.
- [x] Run `rg` checks for stale provider-only wording, broken paths, secrets and trailing whitespace.
- [x] Run `git diff --check` and inspect the complete documentation diff.
- [x] Commit all documentation updates in one commit and push `origin/master`.
