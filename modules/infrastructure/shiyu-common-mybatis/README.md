# shiyu-common-mybatis 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-mybatis`
- **分类**：基础设施模块

## 作用

为仓库提供统一的 MyBatis-Flex 数据访问配置、租户过滤和审计字段基础设施，并适配已配置的数据源。

## 功能说明

- 根据当前可信 `TenantScope` 配置 MyBatis-Flex 租户条件；`TenantConsistencyListener` 校验租户实体写入与作用域的一致性，`AuditFieldListener` 填充审计字段。
- 提供通用 Mapper、持久化模型、类型处理与 `TenantQueryExecutor` 等访问支撑；具体领域表、Repository 和业务查询由领域实现维护。
- 数据库 provider 由 `shiyu.infrastructure.database.provider` 选择；方言根据连接元数据处理。数据库版本、外部 schema 和迁移脚本不由本模块替代管理。
- 不依赖领域 implementation，不把业务表和业务 Repository 放入公共数据访问基础设施。

## 租户数据访问链路

1. `DatabaseInfrastructureProperties` 与数据源配置选择数据库 provider，`MybatisConfig` 装配 MyBatis-Flex 通用行为。
2. `ContextTenantFactory` 从可信 `TenantScope` 提供租户 ID，`TenantFlexConfig` 将它接入 Flex 的租户条件生成；普通仓储不能把请求参数当成第二套租户来源。
3. `ScopeTenantEntity`、`TenantEntity` 与 `TenantConsistencyListener` 约束租户实体写入，`AuditFieldListener` 处理审计字段；业务服务还需校验参数租户、资源归属和对象权限。
4. `TenantQueryExecutor` 只为经过授权的系统级场景提供受控过滤豁免，并负责恢复原状态；普通 CRUD 不应直接关闭 Flex 租户过滤。

`spy.properties` 使用 P6Spy 自身提供的日志工厂和格式化类。数据库异常转 HTTP 响应属于应用 Web 层，位于 `shiyu-ai-web`，不由数据访问模块引入 MVC 依赖。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.mybatis.config`、`com.shiyu.ai.common.mybatis.datasource`、`com.shiyu.ai.common.mybatis.mapper`、`com.shiyu.ai.common.mybatis.model`

## 内部模块依赖

`shiyu-shared-kernel`、`shiyu-common-foundation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-mybatis -am test -Ddependency-check.skip=true`
