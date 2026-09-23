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

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.storage.api`、`com.shiyu.ai.common.storage.backup`、`com.shiyu.ai.common.storage.config`、`com.shiyu.ai.common.storage.file`、`com.shiyu.ai.common.storage.lease`、`com.shiyu.ai.common.storage.metadata`、`com.shiyu.ai.common.storage.rate`、`com.shiyu.ai.common.storage.security` 等

## 内部模块依赖

`shiyu-common-foundation`、`shiyu-common-web`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-storage -am test -Ddependency-check.skip=true`
