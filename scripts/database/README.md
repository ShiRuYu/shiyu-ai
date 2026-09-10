# 数据库迁移与 PostgreSQL 基线

应用启动器只会在 H2 空库上安装本地基线。MySQL、PostgreSQL 是外部托管数据库，启动时只校验 `COMMON_SCHEMA_BASELINE` 的版本和种子标识，不会自动建表或复制数据。切换 provider 前必须先完成一次离线迁移并核对数据量。

应用通过 `shiyu.infrastructure.database.provider`（`h2`、`mysql`、`postgresql`）选择数据库；连接地址、账号和密码只从环境变量或密钥管理系统注入。向量、文件、Redis 和事件 provider 的切换规则见[外部基础设施切换](../../docs/外部基础设施切换.md)。

`postgresql/baseline-schema.sql` 和 `postgresql/baseline-seed.sql` 是按应用启动器资源顺序生成的 PostgreSQL 脚本。脚本将 H2 的 `CACHED TABLE`、内联 `COMMENT`、`TINYINT`、大对象类型和带引号的大写标识符转换为 PostgreSQL 语法，并保留表名、列名和索引语义。源基线变更后，应重新生成这两个文件并进行人工审阅。

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
