# 主分支验证与分支、模块盘点

> 盘点基准：`master` / `d743f4eb`（2026-09-10）。本文件只记录状态和删除建议，不自动删除任何分支。

## 主分支状态

- 当前主分支：`master`，已跟踪 `origin/master`。
- 当前工作树：无未提交变更（文档提交完成后再次确认）。
- Maven reactor：根 POM、29 个叶子模块，共 30 个项目。
- 叶子模块文档：每个模块目录均有 `README.md`。

## 分支用途与删除建议

| 分支 | 最近提交 | 与 master 的关系 | 工作树 | 用途判断 | 删除建议 |
|---|---|---|---|---|---|
| 20260807 | 46ab1793 fix: complete browser E2E fixes and seed knowledge difficulty scale | 已合并（master 领先 32） | 无 | 旧日期快照，浏览器 E2E 与知识难度种子修复 | 本地和远端均可删除；如需审计先打 tag |
| 20260828 | caf77a34 docs: codify architecture and development conventions | 已合并（master 领先 22） | 无 | 旧日期快照，架构与开发规范文档 | 本地可删除 |
| backup/pre-squash-20260908 | 19146586 fix: route implicit app home to local runtime data | 已合并（master 领先 5） | 无 | 压缩提交前的备份指针 | 确认备份保留期后可删除 |
| backup/master-pre-squash-20260909 | be02141f fix: continue exploration beyond initial strategies | 未合并（该分支有 50 个独有提交） | 无 | 主分支压缩前备份，包含尚未进入 master 的持续测试/探索改动 | 暂不可删除；先审查或打 tag |
| codex/update-official-models | f5b6d9c9 refactor: remove obsolete tenant context compatibility | 已合并（master 领先 14） | `.worktrees/shiyu-ai-model-platform-refresh` | 模型平台刷新工作树，提交内容已成为 master 历史 | 工作树使用中，暂不可删除；移除 worktree 后可删除 |
| refactor/domain-vertical-cutover | 2d60ca62 docs: finalize cutover verification | 已合并（master 领先 19） | 无 | 领域垂直切分收尾分支 | 本地可删除 |
| refactor/model-seed-ownership | 681e4257 docs(model): update schema ownership references | 已合并（master 领先 16） | 无 | 模型种子与 schema 所属关系文档 | 本地可删除 |
| refactor/package-boundaries | 26b6193f refactor: decompose iam agent and knowledge services | 未合并（master 领先 3，该分支有 16 个独有提交） | `.worktrees/shiyu-ai-package-boundaries` | 模块边界重构工作树，仍有独有历史 | 暂不可删除；完成合并或明确废弃后再处理 |
| testing/continuous | be02141f fix: continue exploration beyond initial strategies | 未合并（master 领先 4，该分支有 50 个独有提交） | 无；远端同名分支存在 | 持续测试与探索分支，与 backup/master-pre-squash 指向同一提交 | 暂不可删除；先审查独有提交 |
| origin/refactor/api-standardization | d37bb6c6 fix: 后端安全加固 — Token 存储/验证码/限流/XSS/密码等多项修复 | 未合并（该远端分支有 1 个独有提交） | 远端 | API 标准化与安全加固分支 | 暂不可删除；先评估并合并或关闭 PR |

### 已合并日期远端分支

`origin/20260618`、`origin/20260707`、`origin/20260713`、`origin/20260721`、`origin/20260807` 均已完全并入 `master`，没有工作树占用，可以按远端分支保留策略删除。`origin/master` 是主分支，不删除。

### 删除前检查

1. 对候选分支执行 `git log master..<branch>`，确认没有需要保留的提交。
2. 确认没有 PR、发布流水线、定时任务或外部协作仍引用该分支。
3. 先删除本地分支并观察一轮 CI；远端删除需要单独执行 `git push origin --delete <branch>`。
4. 对备份分支优先创建不可变 tag，再删除分支指针。

## Maven 叶子模块清单

| 分类 | 模块 | 说明文档 |
|---|---|---|
| 应用 | `shiyu-ai-bootstrap` | [`README.md`](../../modules/applications/shiyu-ai-bootstrap/README.md) |
| 应用 | `shiyu-ai-web` | [`README.md`](../../modules/applications/shiyu-ai-web/README.md) |
| 应用 | `shiyu-application` | [`README.md`](../../modules/applications/shiyu-application/README.md) |
| 领域契约 | `shiyu-agent-contract` | [`README.md`](../../modules/domains/agent/shiyu-agent-contract/README.md) |
| 领域实现 | `shiyu-agent-implementation` | [`README.md`](../../modules/domains/agent/shiyu-agent-implementation/README.md) |
| 领域契约 | `shiyu-conversation-contract` | [`README.md`](../../modules/domains/conversation/shiyu-conversation-contract/README.md) |
| 领域实现 | `shiyu-conversation-implementation` | [`README.md`](../../modules/domains/conversation/shiyu-conversation-implementation/README.md) |
| 领域契约 | `shiyu-education-contract` | [`README.md`](../../modules/domains/education/shiyu-education-contract/README.md) |
| 领域实现 | `shiyu-education-implementation` | [`README.md`](../../modules/domains/education/shiyu-education-implementation/README.md) |
| 领域契约 | `shiyu-governance-contract` | [`README.md`](../../modules/domains/governance/shiyu-governance-contract/README.md) |
| 领域实现 | `shiyu-governance-implementation` | [`README.md`](../../modules/domains/governance/shiyu-governance-implementation/README.md) |
| 领域契约 | `shiyu-iam-contract` | [`README.md`](../../modules/domains/iam/shiyu-iam-contract/README.md) |
| 领域实现 | `shiyu-iam-implementation` | [`README.md`](../../modules/domains/iam/shiyu-iam-implementation/README.md) |
| 领域契约 | `shiyu-knowledge-contract` | [`README.md`](../../modules/domains/knowledge/shiyu-knowledge-contract/README.md) |
| 领域实现 | `shiyu-knowledge-implementation` | [`README.md`](../../modules/domains/knowledge/shiyu-knowledge-implementation/README.md) |
| 领域契约 | `shiyu-memory-contract` | [`README.md`](../../modules/domains/memory/shiyu-memory-contract/README.md) |
| 领域实现 | `shiyu-memory-implementation` | [`README.md`](../../modules/domains/memory/shiyu-memory-implementation/README.md) |
| 领域契约 | `shiyu-model-contract` | [`README.md`](../../modules/domains/model/shiyu-model-contract/README.md) |
| 领域实现 | `shiyu-model-implementation` | [`README.md`](../../modules/domains/model/shiyu-model-implementation/README.md) |
| 领域契约 | `shiyu-tooling-contract` | [`README.md`](../../modules/domains/tooling/shiyu-tooling-contract/README.md) |
| 领域实现 | `shiyu-tooling-implementation` | [`README.md`](../../modules/domains/tooling/shiyu-tooling-implementation/README.md) |
| 基础设施 | `shiyu-common-core` | [`README.md`](../../modules/infrastructure/shiyu-common-core/README.md) |
| 基础设施 | `shiyu-common-mybatis` | [`README.md`](../../modules/infrastructure/shiyu-common-mybatis/README.md) |
| 基础设施 | `shiyu-common-storage` | [`README.md`](../../modules/infrastructure/shiyu-common-storage/README.md) |
| 基础设施 | `shiyu-common-thread` | [`README.md`](../../modules/infrastructure/shiyu-common-thread/README.md) |
| 基础设施 | `shiyu-common-vector` | [`README.md`](../../modules/infrastructure/shiyu-common-vector/README.md) |
| 基础设施 | `shiyu-common-web` | [`README.md`](../../modules/infrastructure/shiyu-common-web/README.md) |
| 共享内核 | `shiyu-shared-kernel` | [`README.md`](../../modules/shared/shiyu-shared-kernel/README.md) |
| 架构测试 | `shiyu-architecture-tests` | [`README.md`](../../tests/shiyu-architecture-tests/README.md) |

## 验证命令

```powershell
mvn --batch-mode --no-transfer-progress -Pstrict-warnings verify -Ddependency-check.skip=true
python -m unittest discover -s scripts/architecture/tests -p "test_*.py"
python scripts/architecture/inventory_java_packages.py
python scripts/architecture/check_domain_coverage.py
python scripts/architecture/check_legacy_routes.py
python scripts/architecture/check_backend_terminology.py
python scripts/architecture/check_domain_module_dependencies.py
python scripts/architecture/check_schema_ownership.py
python scripts/architecture/check_backend_module_names.py
python scripts/docs/verify_documentation.py
python scripts/verify_fresh_startup.py
```

应用运行验证使用 `modules/applications/shiyu-ai-bootstrap` 的 dev 配置；运行时数据写入 `runtime/`，禁止把凭据写入模块文档或提交。
