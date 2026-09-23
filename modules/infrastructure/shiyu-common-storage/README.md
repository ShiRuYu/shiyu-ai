# shiyu-common-storage 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-storage`
- **分类**：基础设施模块

## 作用

提供文件、短期协调状态与备份等可复用存储适配；文件内容、关系数据库元数据、Redis 和向量存储各自通过明确接口分工。

## 功能说明

- `StorageProperties`（`shiyu.storage`）默认使用本地存储，并支持 `local`、`s3`、`minio`、`aliyun-oss` 和 `tencent-cos` 类型；非本地类型通过 S3 兼容适配器访问。`shiyu.infrastructure.file.provider` 可覆盖旧的 `shiyu.storage.type`。
- 文件管理器提供对象读写、上传/下载与可续传上传支撑；配置 JDBC 元数据 store 时保存租户、空间、对象及上传会话元数据，没有可用元数据 store 时使用 Noop store，不提供持久元数据保证。
- `/api/iam/files` 提供配置查询、列表、上传、下载和删除：配置/列表要求 `file:list`，上传要求 `file:upload`，删除要求 `file:delete`；下载/删除还会验证对象键属于当前租户的命名空间。
- 提供文件安全扫描、存储后端迁移 runner，以及嵌入式数据库备份、清单和恢复前校验能力。迁移不会随普通请求隐式执行。
- Redis 适配提供幂等键、租约和限流等短期状态能力；`shiyu.infrastructure.redis.provider` 默认 `disabled`，此时使用 JVM 内本地实现，本地状态不具备跨实例共享或分布式协调保证。设置为 `redis` 才会装配 Redis provider。
- `VectorIndexStore` 定义按命名空间 upsert/search/delete 的接口；本模块没有该接口的实现。通用向量引擎适配位于 `shiyu-common-vector`。
- 本模块提供存储适配而非租户授权。调用者必须使用可信租户上下文/命名空间；不能假设每种存储后端自动完成租户隔离。

## 文件与短期状态调用链

1. `FileStorageConfiguration` 读取文件 provider 配置；`FileStorageManager` 在 `LocalFileStorage` 和 `S3CompatibleFileStorage` 间选择对象内容存储，并通过 `FileStorageObjectStorage` 适配统一接口。
2. `ResumableUploadService` 管理分片上传会话；`StorageMetadataStore` 的 JDBC/Noop 实现决定元数据是否可持久查询。Noop 不是数据库元数据的等价替代。
3. `FileController` 接收 `/api/iam/files` 请求，在调用存储前执行接口权限和租户对象键检查；文件内容的存放位置不决定用户是否有权访问。
4. `FileStorageMigrationRunner` 只在明确开启迁移配置时搬迁对象；`EmbeddedBackupService` 与 `BackupManifestContributor` 服务于嵌入式数据备份及恢复核对。
5. `IdempotencyStore`、`LeaseStore`、`RateLimitStore` 分别有本地与 Redis 路径。本地实现适合单进程，不提供跨节点一致性。

S3 SDK 在存储模块是可选依赖；可执行应用使用 S3/MinIO 时，还必须启用 Bootstrap 的 `s3` Maven profile。`VectorIndexStore` 只是存储侧端口，不能误认为本模块实现了向量检索引擎。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.storage.api`、`com.shiyu.ai.common.storage.backup`、`com.shiyu.ai.common.storage.config`、`com.shiyu.ai.common.storage.file`、`com.shiyu.ai.common.storage.lease`、`com.shiyu.ai.common.storage.metadata`、`com.shiyu.ai.common.storage.rate`、`com.shiyu.ai.common.storage.security` 等

## 内部模块依赖

`shiyu-common-foundation`、`shiyu-common-web`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-storage -am test -Ddependency-check.skip=true`
