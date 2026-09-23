# shiyu-ai-bootstrap 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-ai-bootstrap`
- **分类**：应用模块

## 作用

提供项目唯一的可执行 Spring Boot 入口 `ShiyuBootstrapApplication`，将平台组合、Web 适配和业务实现装配成一个可运行应用。

## 功能说明

- `application.yml` 和 profile 配置描述 H2 开发运行方式，以及 MySQL、PostgreSQL 和外部基础设施等部署场景；地址、账号和密钥通过外部配置提供。
- 启动阶段校验数据库、文件、向量等 provider 与运行环境是否匹配，避免配置组合错误后继续启动。
- 启动生命周期组件对嵌入式数据目录加锁，避免多个进程同时写入同一份本地数据；日志保留服务按配置管理应用日志。
- 依赖平台组合模块安装或校验数据库基线，并接入数据保留和治理用量事件处理。
- `shiyu.modules.<module-id>.enabled` 控制可选业务模块；本模块只提供唯一启动入口，不承载课程、模型、知识等领域规则。
- 默认开发构建启用 Swagger UI；生产 profile 默认不包含 Swagger UI。S3 兼容对象存储需显式启用 `s3` Maven profile，本地文件存储无需该依赖。

## 启动与运行链路

1. `ShiyuBootstrapApplication` 是唯一的 `main` 入口，应用依赖引入平台组合、Web 入口以及各领域实现；业务模块开关决定可选模块是否装配，而不是启动另一个进程。
2. `EmbeddedDataDirectoryLock` 解析 `APP_HOME` / `app.home`，为本地嵌入式数据目录建立进程锁；多实例部署不能共享同一份 H2、JVector 和本地任务目录。
3. 平台组合层的 `DatabaseInitializer` 安装或校验数据库基线；`ApplicationStartupListener` 在应用就绪后记录可访问入口和启动状态。
4. `LogRetentionService` 按配置执行应用日志保留。数据库、文件、向量等 provider 的实现和业务规则仍由相应模块负责。

## 构建选项

- 默认运行依赖包含 H2、PostgreSQL 和 MySQL 驱动；它们属于运行时依赖，不要求业务代码编译时直接使用驱动 API。
- `-Pprod` 选择生产环境并关闭默认启用的 Swagger UI；显式组合 `-Pprod,api-docs-ui` 才在生产包中加入 UI。
- `-Ps3` 加入 S3 SDK；未启用时应用使用本地文件 provider，不能仅改配置就使用 S3/MinIO。
- `shiyu-common-event` 不在默认应用依赖中；需要 outbox/Kafka 时必须明确装配和配置该可选模块。

## 边界

只有本模块生成可执行应用。部署通过依赖与模块开关组合功能，而不是为每个业务模块创建第二个 application。

## 主要包

`com.shiyu.ai.bootstrap`、`com.shiyu.ai.bootstrap.lifecycle`、`com.shiyu.ai.bootstrap.lock`、`com.shiyu.ai.bootstrap.retention`

## 内部模块依赖

`shiyu-ai-web`、`shiyu-education-implementation`、`shiyu-governance-implementation`、`shiyu-platform-composition`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-bootstrap -am test -Ddependency-check.skip=true`
