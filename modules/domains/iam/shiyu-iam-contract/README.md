# shiyu-iam-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-iam-contract`
- **分类**：领域模块 · Contract

## 作用

定义其他模块向 IAM 查询模块访问能力、租户配置和平台统计授权的契约边界。

## 契约内容

- `TenantModuleAccessPort` 供调用方询问指定租户能否使用某个业务模块。
- `TenantModuleAccessProvisioning` 用于初始化已装配模块的默认启用记录；知识默认设置初始化则由 `shiyu-knowledge-contract` 定义。
- `PlatformUsageAccess` 为平台治理统计等受限操作定义授权检查入口。
- 本模块不处理登录、不分配角色、不读取 Session，也不持有 IAM 数据库或 Sa-Token 实现；这些属于 `shiyu-iam-implementation`。

## 边界

治理和知识等领域可依赖这些协作接口；contract 不依赖 IAM implementation 或 Web 框架。

## 主要包

`com.shiyu.ai.iam.contract`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/iam/shiyu-iam-contract -am test -Ddependency-check.skip=true`
