# shiyu-iam-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-iam-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现用户认证、身份授权、租户管理及平台访问控制，为其余业务模块提供可信主体与租户上下文。

## 功能说明

- 认证与账号：处理登录、验证码、令牌/Session 刷新、密码及账号相关操作，并把认证身份转换为当前请求可验证的主体。
- 授权管理：维护用户、角色、菜单与权限码的关联，为接口权限校验和管理界面提供授权数据。
- 租户与委派：维护租户树、角色分配和租户切换；支持父子租户的授权委派/下钻与返回归属身份，并持久化当前租户 Session 状态。
- 租户扩展：通过 `TenantModuleAccessPort` 提供模块可用性判断，通过租户 provisioning 契约协调新租户默认能力初始化。
- 平台治理授权：实现 `PlatformUsageAccess`，对平台统计访问进行服务端身份、租户及权限检查。
- 辅助管理：维护时区、字典等 IAM 管理数据；H2 schema/seed 与数据库更新脚本提供基线和升级数据。
- IAM Controller 对外提供身份与管理接口；其他领域应依赖 `shiyu-iam-contract`，不得直接依赖本模块的 Repository 或实体。

## 边界

认证身份、当前租户和有效角色由服务端验证，不由客户端提交的租户标识单独决定。领域服务仍须检查具体资源的归属与业务权限。

## 主要包

`com.shiyu.ai.iam.implementation`、`com.shiyu.ai.iam.implementation.api`、`com.shiyu.ai.iam.implementation.application`、`com.shiyu.ai.iam.implementation.config`、`com.shiyu.ai.iam.implementation.domain`、`com.shiyu.ai.iam.implementation.handler`、`com.shiyu.ai.iam.implementation.persistence`、`com.shiyu.ai.iam.implementation.port` 等

## 内部模块依赖

`shiyu-iam-contract`、`shiyu-knowledge-contract`、`shiyu-shared-kernel`、`shiyu-common-web`、`shiyu-common-foundation`、`shiyu-common-mybatis`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/iam/shiyu-iam-implementation -am test -Ddependency-check.skip=true`
