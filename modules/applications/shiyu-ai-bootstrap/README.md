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

## 边界

只有本模块生成可执行应用。部署通过依赖与模块开关组合功能，而不是为每个业务模块创建第二个 application。

## 主要包

`com.shiyu.ai.bootstrap`、`com.shiyu.ai.bootstrap.lifecycle`、`com.shiyu.ai.bootstrap.lock`、`com.shiyu.ai.bootstrap.retention`

## 内部模块依赖

`shiyu-ai-web`、`shiyu-education-implementation`、`shiyu-governance-implementation`、`shiyu-platform-composition`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-bootstrap -am test -Ddependency-check.skip=true`
