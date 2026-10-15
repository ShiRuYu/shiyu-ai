# 数据库迁移与 PostgreSQL 基线

应用启动器只会在 H2 空库上安装本地基线。MySQL、PostgreSQL 是外部托管数据库，启动时只校验 `COMMON_SCHEMA_BASELINE` 的版本和种子标识，不会自动建表或复制数据。切换 provider 前必须先完成一次离线迁移并核对数据量。

应用通过 `shiyu.infrastructure.database.provider`（`h2`、`mysql`、`postgresql`）选择数据库；连接地址、账号和密码只从环境变量或密钥管理系统注入。向量、文件、Redis 和事件 provider 的切换规则见[外部基础设施切换](../../docs/外部基础设施切换.md)。

`postgresql/baseline-schema.sql` 和 `postgresql/baseline-seed.sql` 按应用启动器的 schema、seed、增补资源顺序生成。转换器保留 seed 字符串原文，将 H2 的表、类型和更新时间语法转换为 PostgreSQL 可执行 SQL，并为 `ON UPDATE CURRENT_TIMESTAMP` 生成触发器；seed 最后同步 identity 序列，再写入版本标记。源资源变更后，运行 `python scripts/database/generate-postgresql-baseline.py` 重新生成，并在全新 PostgreSQL 测试库执行完整迁移和应用冒烟。仓库 CI 使用 `scripts/database/verify_postgresql_ci.sh` 做该检查。

`migrate.ps1 -Action baseline` 在一个 PostgreSQL 事务中执行两份脚本；任一语句失败都会回滚，不会留下部分安装的 schema。只在空目标库执行基线安装。

现有外部数据库在升级 Agent 检查点恢复逻辑前，需先增加 `agent_execution.last_checkpoint_id`：PostgreSQL 执行 `postgresql/20261012_checkpoint_pointer.sql`；MySQL 执行一次 `mysql/20261012_checkpoint_pointer.sql`。新建 PostgreSQL 库的基线已包含该列；H2 启动增补脚本会为现有本地库添加该列。迁移前先备份数据库。

在旧库需要使用内置 Agent 应用示例时，PostgreSQL 执行 `postgresql/20261013_sample_apps.sql`，MySQL 执行 `mysql/20261013_sample_apps.sql`。脚本只补缺失的固定 ID；已有同 ID 应用和版本保持原值。H2 在启动时自动执行对应的幂等增补。运行外部数据库脚本前先备份，并在执行后核对 `ai_app` 与 `ai_app_version` 中的 `edu-tutor-app`、`knowledge-qa-app`。

旧版内置 Agent 图仍使用 SILICON_FLOW/Qwen 时，PostgreSQL 执行 `postgresql/20261014_seed_deepseek_graphs.sql`，MySQL 执行 `mysql/20261014_seed_deepseek_graphs.sql`。脚本只替换九个系统图中的旧平台与模型字段，并同步更新 11 条内置 Agent 的旧模型说明；图内其他定制内容及非系统图保持原样。H2 启动时自动执行。执行前备份数据库，执行后核对 `agent_version` 中系统图的模型配置。

旧版外部数据库还需同步教育增补和 DeepSeek 模型目录。MySQL 依次执行 `mysql/20261005_exam_attempt.sql`、`mysql/20261006_question_purpose.sql`、`mysql/20261007_practice_submission.sql`、`mysql/20261007_demo_education_access.sql` 和 `mysql/20261011_deepseek_flash.sql`；PostgreSQL 依次执行 `postgresql/20261005_exam_attempt.sql`、`postgresql/20261006_question_purpose.sql`、`postgresql/20261007_practice_submission.sql`、`postgresql/20261007_demo_education_access.sql` 和 `postgresql/20261011_deepseek_flash.sql`。先备份数据库，并在执行教育 DDL 时停用相关写入。旧库完成后核对考试作答表、练习提交列、`DEMO_EDU_MATH` 访问模式，以及 DeepSeek 模型名 `deepseek-flash` 和 `deepseek-v4-pro`。新 PostgreSQL 基线已包含这些更新；H2 在启动时自动应用资源增补。

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
