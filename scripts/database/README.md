# 数据库迁移与 PostgreSQL 基线

应用启动器会在 H2 或 PostgreSQL 的空库上安装本地基线。PostgreSQL 初始化使用事务级 advisory lock，安装生成的 schema、seed、identity 序列和基线 marker 后执行幂等更新；匹配版本 `4` 与 seed profile `system-ai` 的已有库只运行更新，不重放 seed。非空但缺少 marker、marker 不匹配或 schema 不兼容时，启动会失败且不会重建表。MySQL 仍由部署方预置 schema，启动时只校验 marker、表和必要列。切换 provider 前应先核对目标库和数据量，应用不会跨库复制数据。

应用通过 `shiyu.infrastructure.database.provider`（`h2`、`mysql`、`postgresql`）选择数据库；连接地址、账号和密码只从环境变量或密钥管理系统注入。向量、文件、Redis 和事件 provider 的切换规则见[外部基础设施切换](../../docs/外部基础设施切换.md)。

`postgresql/baseline-schema.sql` 和 `postgresql/baseline-seed.sql` 按应用启动器的 schema、seed、增补资源顺序生成。转换器保留 seed 字符串原文，将 H2 的表、类型和更新时间语法转换为 PostgreSQL 可执行 SQL，并为 `ON UPDATE CURRENT_TIMESTAMP` 生成触发器；seed 最后同步 identity 序列，再写入版本标记。源资源变更后，运行 `python scripts/database/generate-postgresql-baseline.py` 重新生成，并在全新 PostgreSQL 测试库执行完整迁移和应用冒烟。仓库 CI 使用 `scripts/database/verify_postgresql_ci.sh` 做该检查。

`migrate.ps1 -Action baseline` 在一个 PostgreSQL 事务中执行两份脚本；任一语句失败都会回滚，不会留下部分安装的 schema。只在空目标库执行基线安装。

现有外部数据库在升级 Agent 检查点恢复逻辑前，需先增加 `agent_execution.last_checkpoint_id`：PostgreSQL 执行 `postgresql/20261012_checkpoint_pointer.sql`；MySQL 执行一次 `mysql/20261012_checkpoint_pointer.sql`。新建 PostgreSQL 库的基线已包含该列；H2 启动增补脚本会为现有本地库添加该列。迁移前先备份数据库。

在旧库需要使用内置 Agent 应用示例时，PostgreSQL 执行 `postgresql/20261013_sample_apps.sql`，MySQL 执行 `mysql/20261013_sample_apps.sql`。脚本只补缺失的固定 ID；已有同 ID 应用和版本保持原值。H2 在启动时自动执行对应的幂等增补。运行外部数据库脚本前先备份，并在执行后核对 `ai_app` 与 `ai_app_version` 中的 `edu-tutor-app`、`knowledge-qa-app`。

旧版内置 Agent 图仍使用 SILICON_FLOW/Qwen 时，PostgreSQL 执行 `postgresql/20261014_seed_deepseek_graphs.sql`，MySQL 执行 `mysql/20261014_seed_deepseek_graphs.sql`。脚本只替换九个系统图中的旧平台与模型字段，并同步更新 11 条内置 Agent 的旧模型说明；图内其他定制内容及非系统图保持原样。H2 启动时自动执行。执行前备份数据库，执行后核对 `agent_version` 中系统图的模型配置。

旧版外部数据库还需同步教育增补和 DeepSeek 模型目录。MySQL 依次执行 `mysql/20261005_exam_attempt.sql`、`mysql/20261006_question_purpose.sql`、`mysql/20261007_practice_submission.sql`、`mysql/20261007_demo_education_access.sql` 和 `mysql/20261011_deepseek_flash.sql`；PostgreSQL 依次执行 `postgresql/20261005_exam_attempt.sql`、`postgresql/20261006_question_purpose.sql`、`postgresql/20261007_practice_submission.sql`、`postgresql/20261007_demo_education_access.sql` 和 `postgresql/20261011_deepseek_flash.sql`。先备份数据库，并在执行教育 DDL 时停用相关写入。旧库完成后核对考试作答表、练习提交列、`DEMO_EDU_MATH` 访问模式，以及 DeepSeek 模型名 `deepseek-flash` 和 `deepseek-v4-pro`。新 PostgreSQL 基线已包含这些更新；H2 在启动时自动应用资源增补。

## 配额治理持久化迁移

`GOVERNANCE_QUOTA_TENANT` 和 `GOVERNANCE_QUOTA_RESERVATION` 保存跨实例共享的令牌、分钟请求、活动并发和 reservation 状态。新建 H2 库由治理模块基线创建这些表；现有 H2 库在应用启动时执行资源 `classpath:db/updates/governance/20261024_quota_state.sql`。启动器需将该资源注册到增补脚本列表。该升级不删除或重建现有用量记录。

现有 PostgreSQL 数据库在升级应用前执行 `postgresql/20261024_quota_state.sql`；现有 MySQL 数据库执行 `mysql/20261024_quota_state.sql`。可使用 `pwsh ./scripts/database/migrate.ps1 -Action upgrade-quota -Provider postgresql` 或 `pwsh ./scripts/database/migrate.ps1 -Action upgrade-quota -Provider mysql` 执行对应脚本。先备份并确认应用账号有建表、索引和外键权限。新建 PostgreSQL 基线会由 `generate-postgresql-baseline.py` 将同一治理更新并入生成的 baseline schema。

Conversation 终态用量与配额后处理升级：现有 PostgreSQL 数据库执行 `postgresql/20261024_generation_postprocessing.sql`，现有 MySQL 数据库执行 `mysql/20261024_generation_postprocessing.sql`，也可运行 `pwsh ./scripts/database/migrate.ps1 -Action upgrade-generation-postprocessing -Provider postgresql` 或 `-Provider mysql`。H2 在应用启动时自动创建该队列表。完成 generation 的事务会同时写入待处理项，后台调度器按过期 claim token 协调多实例处理配额结算或释放及用量记录；失败时按指数退避重试，直到各步骤都成功。新建 PostgreSQL 基线包含此表。

Conversation 幂等请求指纹升级：现有 PostgreSQL 数据库执行 `postgresql/20261024_conversation_idempotency.sql`，现有 MySQL 数据库执行 `mysql/20261024_conversation_idempotency.sql`，也可运行 `pwsh ./scripts/database/migrate.ps1 -Action upgrade-conversation-idempotency -Provider postgresql` 或 `-Provider mysql`。H2 的现有库在应用启动时自动执行 `classpath:db/updates/conversation/20261024_idempotency_fingerprint.sql`。历史幂等记录保留 NULL 指纹；这些键不能安全地重放，会返回冲突，下一次新请求使用新的键即可。新建数据库的 PostgreSQL 基线包含该列。

Reservation 默认 5 分钟过期，支持通过 `shiyu.usage.quota.reservation-ttl-ms` 调整；过期扫描间隔和首次延迟分别由 `shiyu.usage.quota.expiry-scan-interval-ms` 与 `shiyu.usage.quota.expiry-scan-initial-delay-ms` 控制。时间列按 UTC epoch milliseconds / seconds 记录，避免数据库会话时区对过期和分钟窗口的影响。并发控制依赖租户状态行上的 `SELECT ... FOR UPDATE`，要求支持该语义的 H2、MySQL InnoDB 或 PostgreSQL。

## 使用方式

客户端从环境变量读取连接信息，密码不要写入命令行或仓库：

```powershell
$env:PGHOST = 'db.example.internal'
$env:PGPORT = '5432'
$env:PGDATABASE = 'shiyu'
$env:PGUSER = 'shiyu_app'
$env:PGPASSWORD = '<secret-from-secret-manager>'
pwsh ./scripts/database/migrate.ps1 -Action baseline -Provider postgresql
```

备份和恢复使用同一个脚本。`pg_dump` 使用 custom 格式，恢复前先停写并在目标库执行完整性核对：

```powershell
pwsh ./scripts/database/migrate.ps1 -Action dump -Provider postgresql -File .\backup\shiyu-db.dump
pwsh ./scripts/database/migrate.ps1 -Action restore -Provider postgresql -File .\backup\shiyu-db.dump
```

MySQL 使用 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PWD` 环境变量和 `mysqldump`/`mysql` 客户端。仓库不提供从空 MySQL 库自动建表的脚本；应使用经过验证的 MySQL 结构转储，然后写入：

```sql
INSERT INTO common_schema_baseline (id, baseline_version, seed_profile)
VALUES (1, '4', 'system-ai');
```

恢复后检查应用期望的表和 `MODEL_AI_PLATFORM.ADAPTER_TYPE` 列，再把 `shiyu.infrastructure.database.provider` 切换为目标数据库。运行时 provider 切换不会自动迁移数据；回滚时恢复原连接配置即可。
