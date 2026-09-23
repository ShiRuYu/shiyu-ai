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

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.mybatis.config`、`com.shiyu.ai.common.mybatis.datasource`、`com.shiyu.ai.common.mybatis.mapper`、`com.shiyu.ai.common.mybatis.model`

## 内部模块依赖

`shiyu-shared-kernel`、`shiyu-common-foundation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-mybatis -am test -Ddependency-check.skip=true`
